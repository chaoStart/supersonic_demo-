package com.tencent.supersonic.demo.mapper;

import com.tencent.supersonic.demo.common.pojo.Constants;
import com.tencent.supersonic.demo.common.util.ContextUtils;
import com.tencent.supersonic.demo.common.util.EditDistanceUtils;
import com.tencent.supersonic.demo.mapper.context.ChatQueryContext;
import com.tencent.supersonic.demo.mapper.helper.HanlpHelper;
import com.tencent.supersonic.demo.mapper.helper.NatureHelper;
import com.tencent.supersonic.demo.mapper.knowledge.DatabaseMapResult;
import com.tencent.supersonic.demo.mapper.knowledge.DictWord;
import com.tencent.supersonic.demo.mapper.knowledge.HanlpMapResult;
import com.tencent.supersonic.demo.mapper.knowledge.KnowledgeBaseService;
import com.tencent.supersonic.demo.semantic.enums.SchemaElementType;
import com.tencent.supersonic.demo.semantic.pojo.SchemaElement;
import com.tencent.supersonic.demo.semantic.pojo.SchemaElementMatch;
import com.tencent.supersonic.demo.semantic.pojo.SchemaMapInfo;
import com.tencent.supersonic.demo.semantic.pojo.SchemaValueMap;
import com.tencent.supersonic.demo.semantic.response.S2Term;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 关键词映射器
 * 使用HanLP词典匹配和数据库匹配两种策略识别schema元素
 */
@Slf4j
@Component
public class KeywordMapper extends BaseMapper {

    @Override
    public void doMap(ChatQueryContext chatQueryContext) {
        String queryText = chatQueryContext.getRequest().getQueryText();
        if (StringUtils.isBlank(queryText)) {
            return;
        }

        // 1. 分词
        List<S2Term> terms = HanlpHelper.getTerms(queryText, chatQueryContext.getModelIdToDataSetIds());

        // 2. HanLP词典匹配
        KnowledgeBaseService knowledgeBaseService = ContextUtils.getBean(KnowledgeBaseService.class);
        List<HanlpMapResult> hanlpMatchResults = knowledgeBaseService.prefixSearch(
                queryText,
                chatQueryContext.getModelIdToDataSetIds(),
                chatQueryContext.getRequest().getDataSetIds()
        );
        convertHanlpResultToMapInfo(hanlpMatchResults, chatQueryContext, terms);

        // 3. 数据库匹配（直接从Schema中搜索）
        List<DatabaseMapResult> databaseMatchResults = databaseMatch(chatQueryContext);
        convertDatabaseResultToMapInfo(chatQueryContext, databaseMatchResults);
    }

    /**
     * 将HanLP匹配结果转换为MapInfo
     */
    private void convertHanlpResultToMapInfo(List<HanlpMapResult> mapResults,
                                              ChatQueryContext chatQueryContext,
                                              List<S2Term> terms) {
        if (CollectionUtils.isEmpty(mapResults)) {
            return;
        }

        HanlpHelper.transLetterOriginal(mapResults);
        Map<String, Long> wordNatureToFrequency = terms.stream()
                .collect(Collectors.toMap(
                        term -> term.getWord() + term.getNature(),
                        term -> (long) term.getFrequency(),
                        (value1, value2) -> value2));

        for (HanlpMapResult hanlpMapResult : mapResults) {
            for (String nature : hanlpMapResult.getNatures()) {
                Long dataSetId = NatureHelper.getDataSetId(nature);
                if (Objects.isNull(dataSetId)) {
                    continue;
                }
                SchemaElementType elementType = NatureHelper.convertToElementType(nature);
                if (Objects.isNull(elementType)) {
                    continue;
                }
                Long elementID = NatureHelper.getElementID(nature);
                if (elementID == null) {
                    continue;
                }
                SchemaElement element = getSchemaElement(dataSetId, elementType, elementID,
                        chatQueryContext.getSemanticSchema());
                if (Objects.isNull(element)) {
                    continue;
                }

                Long frequency = wordNatureToFrequency.get(hanlpMapResult.getName() + nature);
                SchemaElementMatch schemaElementMatch = SchemaElementMatch.builder()
                        .element(element)
                        .frequency(frequency)
                        .word(hanlpMapResult.getName())
                        .similarity(hanlpMapResult.getSimilarity())
                        .detectWord(hanlpMapResult.getDetectWord())
                        .build();

                // 处理维度值别名
                doDimValueAliasLogic(schemaElementMatch, chatQueryContext.getSemanticSchema().getDimensionValues());
                addToSchemaMap(chatQueryContext.getMapInfo(), dataSetId, schemaElementMatch);
            }
        }
    }

    /**
     * 处理维度值别名逻辑
     */
    private void doDimValueAliasLogic(SchemaElementMatch schemaElementMatch, List<SchemaElement> dimensionValues) {
        SchemaElement element = schemaElementMatch.getElement();
        if (SchemaElementType.VALUE.equals(element.getType())) {
            Long dimId = element.getId();
            String word = schemaElementMatch.getWord();
            Map<Long, List<DictWord>> dimValueAlias = KnowledgeBaseService.getDimValueAlias();
            if (Objects.nonNull(dimId) && StringUtils.isNotEmpty(word) && dimValueAlias.containsKey(dimId)) {
                Map<String, DictWord> aliasAndDictMap = dimValueAlias.get(dimId).stream()
                        .collect(Collectors.toMap(DictWord::getAlias, dictWord -> dictWord, (v1, v2) -> v2));
                if (aliasAndDictMap.containsKey(word)) {
                    String wordTech = aliasAndDictMap.get(word).getWord();
                    schemaElementMatch.setWord(wordTech);
                }
            }
            SchemaElement dimensionValue = dimensionValues.stream()
                    .filter(dimValue -> dimId.equals(dimValue.getId()))
                    .findFirst()
                    .orElse(null);
            if (dimensionValue != null && dimensionValue.getSchemaValueMaps() != null) {
                SchemaValueMap dimValue = dimensionValue.getSchemaValueMaps().stream()
                        .filter(schemaValueMap -> StringUtils.equals(schemaValueMap.getBizName(), word)
                                || (schemaValueMap.getAlias() != null && schemaValueMap.getAlias().contains(word)))
                        .findFirst()
                        .orElse(null);
                if (dimValue != null) {
                    schemaElementMatch.setWord(dimValue.getTechName());
                }
            }
        }
    }

    /**
     * 数据库匹配 - 从Schema中搜索匹配的元素
     */
    private List<DatabaseMapResult> databaseMatch(ChatQueryContext chatQueryContext) {
        String queryText = chatQueryContext.getRequest().getQueryText().toLowerCase();
        Set<Long> dataSetIds = chatQueryContext.getRequest().getDataSetIds();
        MapperConfig config = ContextUtils.getBean(MapperConfig.class);
        double threshold = config.getMapperNameThreshold();

        List<DatabaseMapResult> results = new java.util.ArrayList<>();

        // 从语义Schema中获取所有维度和指标
        List<SchemaElement> allElements = new java.util.ArrayList<>();
        allElements.addAll(chatQueryContext.getSemanticSchema().getDimensions());
        allElements.addAll(chatQueryContext.getSemanticSchema().getMetrics());

        // 过滤数据集
        if (!CollectionUtils.isEmpty(dataSetIds)) {
            allElements = allElements.stream()
                    .filter(e -> dataSetIds.contains(e.getDataSetId()))
                    .collect(Collectors.toList());
        }

        // 对每个元素进行匹配
        for (SchemaElement element : allElements) {
            // 匹配名称
            double similarity = EditDistanceUtils.getSimilarity(queryText, element.getName().toLowerCase());
            if (similarity >= threshold && queryText.contains(element.getName().toLowerCase())) {
                results.add(new DatabaseMapResult(element.getName(), element.getName(), element, similarity));
            }

            // 匹配别名
            if (element.getAlias() != null) {
                for (String alias : element.getAlias()) {
                    similarity = EditDistanceUtils.getSimilarity(queryText, alias.toLowerCase());
                    if (similarity >= threshold && queryText.contains(alias.toLowerCase())) {
                        results.add(new DatabaseMapResult(alias, alias, element, similarity));
                    }
                }
            }
        }

        return results;
    }

    /**
     * 将数据库匹配结果转换为MapInfo
     */
    private void convertDatabaseResultToMapInfo(ChatQueryContext chatQueryContext,
                                                 List<DatabaseMapResult> mapResults) {
        if (CollectionUtils.isEmpty(mapResults)) {
            return;
        }

        for (DatabaseMapResult match : mapResults) {
            SchemaElement schemaElement = match.getSchemaElement();
            Set<Long> regElementSet = getRegElementSet(chatQueryContext.getMapInfo(), schemaElement);
            if (regElementSet.contains(schemaElement.getId())) {
                continue;
            }
            SchemaElementMatch schemaElementMatch = SchemaElementMatch.builder()
                    .element(schemaElement)
                    .word(schemaElement.getName())
                    .detectWord(match.getDetectWord())
                    .frequency(Constants.DEFAULT_FREQUENCY)
                    .similarity(EditDistanceUtils.getSimilarity(match.getDetectWord(), schemaElement.getName()))
                    .build();
            log.debug("add to schema, elementMatch {}", schemaElementMatch);
            addToSchemaMap(chatQueryContext.getMapInfo(), schemaElement.getDataSetId(), schemaElementMatch);
        }
    }

    /**
     * 获取已注册的元素ID集合
     */
    private Set<Long> getRegElementSet(SchemaMapInfo schemaMap, SchemaElement schemaElement) {
        List<SchemaElementMatch> elements = schemaMap.getMatchedElements(schemaElement.getDataSetId());
        if (CollectionUtils.isEmpty(elements)) {
            return new HashSet<>();
        }
        return elements.stream()
                .filter(elementMatch -> SchemaElementType.METRIC.equals(elementMatch.getElement().getType())
                        || SchemaElementType.DIMENSION.equals(elementMatch.getElement().getType()))
                .map(elementMatch -> elementMatch.getElement().getId())
                .collect(Collectors.toSet());
    }
}

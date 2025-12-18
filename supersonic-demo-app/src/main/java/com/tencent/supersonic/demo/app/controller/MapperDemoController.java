package com.tencent.supersonic.demo.app.controller;

import com.tencent.supersonic.demo.mapper.KeywordMapper;
import com.tencent.supersonic.demo.mapper.builder.DimensionWordBuilder;
import com.tencent.supersonic.demo.mapper.builder.MetricWordBuilder;
import com.tencent.supersonic.demo.mapper.builder.ValueWordBuilder;
import com.tencent.supersonic.demo.mapper.context.ChatQueryContext;
import com.tencent.supersonic.demo.mapper.context.QueryNLReq;
import com.tencent.supersonic.demo.mapper.knowledge.DictWord;
import com.tencent.supersonic.demo.mapper.knowledge.KnowledgeBaseService;
import com.tencent.supersonic.demo.semantic.enums.MapModeEnum;
import com.tencent.supersonic.demo.semantic.pojo.SchemaElement;
import com.tencent.supersonic.demo.semantic.pojo.SchemaElementMatch;
import com.tencent.supersonic.demo.semantic.pojo.SchemaMapInfo;
import com.tencent.supersonic.demo.semantic.pojo.SemanticSchema;
import com.tencent.supersonic.demo.semantic.service.SemanticSchemaService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper演示控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/mapper")
public class MapperDemoController {

    @Autowired
    private KeywordMapper keywordMapper;

    @Autowired
    private SemanticSchemaService semanticSchemaService;

    @Autowired
    private KnowledgeBaseService knowledgeBaseService;

    @Autowired
    private DimensionWordBuilder dimensionWordBuilder;

    @Autowired
    private MetricWordBuilder metricWordBuilder;

    @Autowired
    private ValueWordBuilder valueWordBuilder;

    /**
     * 初始化知识库 - 在应用完全启动后执行
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        reloadKnowledgeBase();
    }

    /**
     * 执行字段映射
     */
    @PostMapping("/map")
    public MapResponse map(@RequestBody MapRequest request) {
        log.info("Received map request: {}", request);

        // 构建查询上下文
        QueryNLReq queryReq = QueryNLReq.builder()
                .queryText(request.getQueryText())
                .dataSetIds(request.getDataSetIds() != null ? request.getDataSetIds() : new HashSet<>())
                .mapModeEnum(request.getMapMode() != null ? request.getMapMode() : MapModeEnum.STRICT)
                .build();

        ChatQueryContext context = new ChatQueryContext(queryReq);
        context.setSemanticSchema(semanticSchemaService.getSemanticSchema());
        context.setModelIdToDataSetIds(semanticSchemaService.getModelIdToDataSetIds());

        // 执行映射
        keywordMapper.map(context);

        // 转换响应
        return convertToResponse(context.getMapInfo(), request.getQueryText());
    }

    /**
     * 获取数据集Schema信息
     */
    @GetMapping("/schema")
    public SchemaResponse getSchema(@RequestParam(required = false) Long dataSetId) {
        SemanticSchema schema = semanticSchemaService.getSemanticSchema();
        SchemaResponse response = new SchemaResponse();

        if (dataSetId != null) {
            response.setDataSets(schema.getDataSets().stream()
                    .filter(ds -> dataSetId.equals(ds.getId()))
                    .collect(Collectors.toList()));
            response.setDimensions(schema.getDimensions(dataSetId));
            response.setMetrics(schema.getMetrics(dataSetId));
        } else {
            response.setDataSets(schema.getDataSets());
            response.setDimensions(schema.getDimensions());
            response.setMetrics(schema.getMetrics());
        }

        return response;
    }

    /**
     * 重新加载知识库
     */
    @PostMapping("/reload")
    public String reload() {
        semanticSchemaService.reload();
        reloadKnowledgeBase();
        return "Knowledge base reloaded successfully";
    }

    /**
     * 重新加载知识库
     */
    private void reloadKnowledgeBase() {
        log.info("Reloading knowledge base...");
        knowledgeBaseService.clear();

        SemanticSchema schema = semanticSchemaService.getSemanticSchema();
        if (schema == null) {
            log.warn("Semantic schema is null, skipping knowledge base reload");
            return;
        }

        // 构建维度词
        List<DictWord> dimensionWords = dimensionWordBuilder.build(schema.getDimensions());
        knowledgeBaseService.updateKnowledge(dimensionWords);

        // 构建指标词
        List<DictWord> metricWords = metricWordBuilder.build(schema.getMetrics());
        knowledgeBaseService.updateKnowledge(metricWords);

        // 构建维度值词
        List<DictWord> valueWords = valueWordBuilder.build(schema.getDimensionValues());
        knowledgeBaseService.updateKnowledge(valueWords);

        log.info("Knowledge base reloaded: {} dimensions, {} metrics, {} values",
                dimensionWords.size(), metricWords.size(), valueWords.size());
    }

    /**
     * 转换响应
     */
    private MapResponse convertToResponse(SchemaMapInfo mapInfo, String queryText) {
        MapResponse response = new MapResponse();
        response.setQueryText(queryText);

        List<MatchResult> matches = new ArrayList<>();
        for (Map.Entry<Long, List<SchemaElementMatch>> entry : mapInfo.getDataSetElementMatches().entrySet()) {
            Long dataSetId = entry.getKey();
            for (SchemaElementMatch match : entry.getValue()) {
                SchemaElement element = match.getElement();
                matches.add(MatchResult.builder()
                        .dataSetId(dataSetId)
                        .elementId(element.getId())
                        .elementName(element.getName())
                        .elementBizName(element.getBizName())
                        .elementType(element.getType().name())
                        .detectWord(match.getDetectWord())
                        .matchWord(match.getWord())
                        .similarity(match.getSimilarity())
                        .build());
            }
        }
        response.setMatches(matches);
        response.setMatchCount(matches.size());

        return response;
    }

    // ==================== Request/Response DTOs ====================

    @Data
    public static class MapRequest {
        private String queryText;
        private Set<Long> dataSetIds;
        private MapModeEnum mapMode;
    }

    @Data
    public static class MapResponse {
        private String queryText;
        private int matchCount;
        private List<MatchResult> matches;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MatchResult {
        private Long dataSetId;
        private Long elementId;
        private String elementName;
        private String elementBizName;
        private String elementType;
        private String detectWord;
        private String matchWord;
        private double similarity;
    }

    @Data
    public static class SchemaResponse {
        private List<SchemaElement> dataSets;
        private List<SchemaElement> dimensions;
        private List<SchemaElement> metrics;
    }
}

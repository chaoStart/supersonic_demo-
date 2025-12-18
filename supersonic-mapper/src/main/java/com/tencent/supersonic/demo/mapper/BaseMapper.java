package com.tencent.supersonic.demo.mapper;

import com.tencent.supersonic.demo.mapper.context.ChatQueryContext;
import com.tencent.supersonic.demo.semantic.enums.SchemaElementType;
import com.tencent.supersonic.demo.semantic.pojo.DataSetSchema;
import com.tencent.supersonic.demo.semantic.pojo.SchemaElement;
import com.tencent.supersonic.demo.semantic.pojo.SchemaElementMatch;
import com.tencent.supersonic.demo.semantic.pojo.SchemaMapInfo;
import com.tencent.supersonic.demo.semantic.pojo.SemanticSchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * 基础映射器抽象类
 */
@Slf4j
public abstract class BaseMapper implements SchemaMapper {

    @Override
    public void map(ChatQueryContext chatQueryContext) {
        if (!accept(chatQueryContext)) {
            return;
        }

        String simpleName = this.getClass().getSimpleName();
        long startTime = System.currentTimeMillis();
        log.debug("before {}, mapInfo:{}", simpleName, chatQueryContext.getMapInfo().getDataSetElementMatches());

        try {
            doMap(chatQueryContext);
        } catch (Exception e) {
            log.error("mapper error", e);
        }

        long cost = System.currentTimeMillis() - startTime;
        log.debug("after {}, cost:{}, mapInfo:{}", simpleName, cost, chatQueryContext.getMapInfo().getDataSetElementMatches());
    }

    /**
     * 执行映射逻辑，由子类实现
     */
    public abstract void doMap(ChatQueryContext chatQueryContext);

    /**
     * 判断是否接受处理
     */
    protected boolean accept(ChatQueryContext chatQueryContext) {
        return true;
    }

    /**
     * 添加匹配结果到SchemaMap
     */
    public void addToSchemaMap(SchemaMapInfo schemaMap, Long dataSetId, SchemaElementMatch newElementMatch) {
        Map<Long, List<SchemaElementMatch>> dataSetElementMatches = schemaMap.getDataSetElementMatches();
        List<SchemaElementMatch> schemaElementMatches = dataSetElementMatches.computeIfAbsent(dataSetId, k -> new ArrayList<>());

        AtomicBoolean shouldAddNew = new AtomicBoolean(true);

        schemaElementMatches.removeIf(existingElementMatch -> {
            if (isEquals(existingElementMatch, newElementMatch)) {
                if (newElementMatch.getSimilarity() > existingElementMatch.getSimilarity()) {
                    return true;
                } else {
                    shouldAddNew.set(false);
                }
            }
            return false;
        });

        if (shouldAddNew.get()) {
            schemaElementMatches.add(newElementMatch);
        }
    }

    /**
     * 比较两个匹配结果是否相等
     */
    private static boolean isEquals(SchemaElementMatch existElementMatch, SchemaElementMatch newElementMatch) {
        SchemaElement existElement = existElementMatch.getElement();
        SchemaElement newElement = newElementMatch.getElement();
        if (!existElement.equals(newElement)) {
            return false;
        }
        if (SchemaElementType.VALUE.equals(newElement.getType())) {
            return existElementMatch.getWord().equalsIgnoreCase(newElementMatch.getWord());
        }
        return true;
    }

    /**
     * 根据ID获取Schema元素
     */
    public SchemaElement getSchemaElement(Long dataSetId, SchemaElementType elementType,
                                          Long elementID, SemanticSchema semanticSchema) {
        SchemaElement element = new SchemaElement();
        DataSetSchema dataSetSchema = semanticSchema.getDataSetSchemaMap().get(dataSetId);
        if (Objects.isNull(dataSetSchema)) {
            return null;
        }
        SchemaElement elementDb = dataSetSchema.getElement(elementType, elementID);
        if (Objects.isNull(elementDb)) {
            return null;
        }
        BeanUtils.copyProperties(elementDb, element);
        element.setAlias(getAlias(elementDb));
        return element;
    }

    /**
     * 获取元素的别名
     */
    public List<String> getAlias(SchemaElement element) {
        if (!SchemaElementType.VALUE.equals(element.getType())) {
            return element.getAlias();
        }
        if (element.getAlias() != null && !element.getAlias().isEmpty() && element.getName() != null) {
            return element.getAlias().stream()
                    .filter(aliasItem -> aliasItem.contains(element.getName()))
                    .collect(Collectors.toList());
        }
        return element.getAlias();
    }
}

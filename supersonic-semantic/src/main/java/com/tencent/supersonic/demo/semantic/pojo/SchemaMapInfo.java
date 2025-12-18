package com.tencent.supersonic.demo.semantic.pojo;

import com.google.common.collect.Lists;
import lombok.Getter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Schema映射信息
 * 存储查询文本映射到语义元素的结果
 */
@Getter
public class SchemaMapInfo implements Serializable {

    /**
     * 数据集ID -> 匹配到的Schema元素列表
     */
    private final Map<Long, List<SchemaElementMatch>> dataSetElementMatches = new HashMap<>();

    /**
     * 判断是否为空
     */
    public boolean isEmpty() {
        return dataSetElementMatches.keySet().isEmpty();
    }

    /**
     * 获取所有匹配到的数据集ID
     */
    public Set<Long> getMatchedDataSetInfos() {
        return dataSetElementMatches.keySet();
    }

    /**
     * 获取指定数据集的匹配元素列表
     */
    public List<SchemaElementMatch> getMatchedElements(Long dataSet) {
        return dataSetElementMatches.getOrDefault(dataSet, Lists.newArrayList());
    }

    /**
     * 设置指定数据集的匹配元素列表
     */
    public void setMatchedElements(Long dataSet, List<SchemaElementMatch> elementMatches) {
        dataSetElementMatches.put(dataSet, elementMatches);
    }

    /**
     * 合并其他SchemaMapInfo的匹配结果
     */
    public void addMatchedElements(SchemaMapInfo schemaMapInfo) {
        for (Map.Entry<Long, List<SchemaElementMatch>> entry : schemaMapInfo.dataSetElementMatches.entrySet()) {
            Long dataSet = entry.getKey();
            List<SchemaElementMatch> newMatches = entry.getValue();

            if (dataSetElementMatches.containsKey(dataSet)) {
                List<SchemaElementMatch> existingMatches = dataSetElementMatches.get(dataSet);
                Set<SchemaElementMatch> mergedMatches = new HashSet<>(existingMatches);
                mergedMatches.addAll(newMatches);
                dataSetElementMatches.put(dataSet, new ArrayList<>(mergedMatches));
            } else {
                dataSetElementMatches.put(dataSet, new ArrayList<>(new HashSet<>(newMatches)));
            }
        }
    }
}

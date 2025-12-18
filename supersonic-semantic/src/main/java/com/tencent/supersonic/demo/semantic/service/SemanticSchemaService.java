package com.tencent.supersonic.demo.semantic.service;

import com.tencent.supersonic.demo.semantic.pojo.DataSetSchema;
import com.tencent.supersonic.demo.semantic.pojo.SemanticSchema;

import java.util.Map;
import java.util.List;

/**
 * 语义Schema服务接口
 */
public interface SemanticSchemaService {

    /**
     * 获取完整的语义Schema
     */
    SemanticSchema getSemanticSchema();

    /**
     * 获取指定数据集的Schema
     */
    DataSetSchema getDataSetSchema(Long dataSetId);

    /**
     * 获取模型ID到数据集ID的映射
     */
    Map<Long, List<Long>> getModelIdToDataSetIds();

    /**
     * 重新加载语义Schema
     */
    void reload();
}

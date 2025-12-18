package com.tencent.supersonic.demo.mapper.context;

import com.tencent.supersonic.demo.semantic.pojo.DataSetSchema;
import com.tencent.supersonic.demo.semantic.pojo.SchemaMapInfo;
import com.tencent.supersonic.demo.semantic.pojo.SemanticSchema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 查询上下文
 * 包含查询的所有上下文信息
 */
@Data
public class ChatQueryContext implements Serializable {

    /**
     * 查询请求
     */
    private QueryNLReq request;

    /**
     * 模型ID到数据集ID的映射
     */
    private Map<Long, List<Long>> modelIdToDataSetIds;

    /**
     * Schema映射信息（匹配结果）
     */
    private SchemaMapInfo mapInfo = new SchemaMapInfo();

    /**
     * 语义Schema
     */
    private SemanticSchema semanticSchema;

    public ChatQueryContext() {
        this(new QueryNLReq());
    }

    public ChatQueryContext(QueryNLReq request) {
        this.request = request;
    }

    /**
     * 获取指定数据集的Schema
     */
    public DataSetSchema getDataSetSchema(Long dataSetId) {
        return semanticSchema.getDataSetSchema(dataSetId);
    }

    /**
     * 判断数据集是否包含分区时间维度
     */
    public boolean containsPartitionDimensions(Long dataSetId) {
        DataSetSchema dataSetSchema = semanticSchema.getDataSetSchemaMap().get(dataSetId);
        return dataSetSchema != null && dataSetSchema.containsPartitionDimensions();
    }
}

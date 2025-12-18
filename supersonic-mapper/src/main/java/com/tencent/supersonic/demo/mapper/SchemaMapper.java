package com.tencent.supersonic.demo.mapper;

import com.tencent.supersonic.demo.mapper.context.ChatQueryContext;

/**
 * Schema映射器接口
 * 识别用户查询中引用的schema元素（指标/维度/实体/值）
 */
public interface SchemaMapper {

    /**
     * 执行映射
     *
     * @param chatQueryContext 查询上下文
     */
    void map(ChatQueryContext chatQueryContext);
}

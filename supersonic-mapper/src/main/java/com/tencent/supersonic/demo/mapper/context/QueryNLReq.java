package com.tencent.supersonic.demo.mapper.context;

import com.tencent.supersonic.demo.semantic.enums.MapModeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * 自然语言查询请求
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QueryNLReq implements Serializable {

    /**
     * 查询文本
     */
    private String queryText;

    /**
     * 数据集ID列表
     */
    @Builder.Default
    private Set<Long> dataSetIds = new HashSet<>();

    /**
     * 映射模式
     */
    @Builder.Default
    private MapModeEnum mapModeEnum = MapModeEnum.STRICT;
}

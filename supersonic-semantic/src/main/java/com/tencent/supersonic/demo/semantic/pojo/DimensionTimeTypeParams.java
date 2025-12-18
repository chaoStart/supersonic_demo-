package com.tencent.supersonic.demo.semantic.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 维度时间类型参数
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DimensionTimeTypeParams implements Serializable {
    private String isPrimary;       // 是否主时间
    private String timeGranularity; // 时间粒度
}

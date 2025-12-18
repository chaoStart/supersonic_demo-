package com.tencent.supersonic.demo.mapper;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 映射配置
 */
@Data
@Component
public class MapperConfig {

    /**
     * 一次探测返回结果个数
     */
    @Value("${mapper.detection.size:8}")
    private int mapperDetectionSize;

    /**
     * 一次前后缀匹配返回个数
     */
    @Value("${mapper.detection.max-size:20}")
    private int mapperDetectionMaxSize;

    /**
     * 指标/维度名相似度阈值
     */
    @Value("${mapper.name.threshold:0.3}")
    private double mapperNameThreshold;

    /**
     * 最小相似度阈值
     */
    @Value("${mapper.name.threshold-min:0.25}")
    private double mapperNameThresholdMin;

    /**
     * 维度值返回个数
     */
    @Value("${mapper.dimension-value.size:1}")
    private int mapperDimensionValueSize;

    /**
     * 维度值相似度阈值
     */
    @Value("${mapper.value.threshold:0.5}")
    private double mapperValueThreshold;

    /**
     * 维度值最小相似度阈值
     */
    @Value("${mapper.value.threshold-min:0.3}")
    private double mapperValueThresholdMin;
}

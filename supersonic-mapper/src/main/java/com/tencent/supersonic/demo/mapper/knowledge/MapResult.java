package com.tencent.supersonic.demo.mapper.knowledge;

import lombok.Data;

import java.io.Serializable;

/**
 * 匹配结果基类
 */
@Data
public abstract class MapResult implements Serializable {

    protected String name;        // 元素名称
    protected int offset;         // 偏移位置
    protected String detectWord;  // 检测词
    protected double similarity;  // 相似度

    /**
     * 获取映射键，用于去重
     */
    public abstract String getMapKey();
}

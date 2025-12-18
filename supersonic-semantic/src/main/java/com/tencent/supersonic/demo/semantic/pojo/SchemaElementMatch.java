package com.tencent.supersonic.demo.semantic.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * Schema元素匹配结果
 * 表示一个查询词与语义元素的匹配结果
 */
@Data
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SchemaElementMatch implements Serializable {

    private SchemaElement element;    // 匹配到的Schema元素
    private double offset;            // 在文本中的偏移位置
    private double similarity;        // 相似度(0.0-1.0)
    private String detectWord;        // 检测词(原文本片段)
    private String word;              // 匹配词(元素名称或别名)
    private Long frequency;           // 词频
    private boolean isInherited;      // 是否继承而来

    /**
     * 判断是否完全匹配
     */
    public boolean isFullMatched() {
        return 1.0 == similarity;
    }
}

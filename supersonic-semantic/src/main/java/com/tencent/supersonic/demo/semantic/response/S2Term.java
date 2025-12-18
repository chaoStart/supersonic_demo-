package com.tencent.supersonic.demo.semantic.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 分词结果
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class S2Term implements Serializable {
    private String word;        // 词
    private String nature;      // 词性
    private int frequency;      // 频率
    private int offset;         // 偏移量
}

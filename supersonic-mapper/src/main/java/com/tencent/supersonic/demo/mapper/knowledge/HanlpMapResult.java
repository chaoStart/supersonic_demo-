package com.tencent.supersonic.demo.mapper.knowledge;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * HanLP匹配结果
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class HanlpMapResult extends MapResult {

    /**
     * 词性列表
     */
    private List<String> natures;

    public HanlpMapResult(String name, List<String> natures, String detectWord, double similarity) {
        this.name = name;
        this.natures = natures;
        this.detectWord = detectWord;
        this.similarity = similarity;
    }

    @Override
    public String getMapKey() {
        return name + natures;
    }
}

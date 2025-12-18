package com.tencent.supersonic.demo.mapper.builder;

import com.tencent.supersonic.demo.common.enums.DictWordType;
import com.tencent.supersonic.demo.mapper.knowledge.DictWord;
import com.tencent.supersonic.demo.semantic.pojo.SchemaElement;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 词构建器基类
 */
public abstract class BaseWordBuilder {

    public static final Long DEFAULT_FREQUENCY = 100000L;

    /**
     * 构建词典词列表
     */
    public abstract List<DictWord> build(List<SchemaElement> elements);

    /**
     * 获取词类型
     */
    public abstract DictWordType getWordType();

    /**
     * 构建词性
     */
    protected String buildNature(Long modelId, Long elementId) {
        return DictWordType.NATURE_SPILT + modelId
                + DictWordType.NATURE_SPILT + elementId
                + getWordType().getType();
    }

    /**
     * 构建带频率的词性
     */
    protected String buildNatureWithFrequency(Long modelId, Long elementId) {
        return buildNatureWithFrequency(modelId, elementId, DEFAULT_FREQUENCY);
    }

    /**
     * 构建带频率的词性
     */
    protected String buildNatureWithFrequency(Long modelId, Long elementId, Long frequency) {
        return buildNature(modelId, elementId) + DictWordType.SPACE + frequency;
    }

    /**
     * 添加别名词
     */
    protected void addAliasWords(List<DictWord> words, SchemaElement element) {
        if (element.getAlias() == null || element.getAlias().isEmpty()) {
            return;
        }
        Long modelId = element.getModel();
        Long elementId = element.getId();

        for (String alias : element.getAlias()) {
            if (StringUtils.isNotBlank(alias)) {
                words.add(DictWord.builder()
                        .word(alias.toLowerCase())
                        .nature(buildNature(modelId, elementId))
                        .natureWithFrequency(buildNatureWithFrequency(modelId, elementId))
                        .build());
            }
        }
    }
}

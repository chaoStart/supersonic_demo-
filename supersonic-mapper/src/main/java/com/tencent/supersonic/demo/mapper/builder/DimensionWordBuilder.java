package com.tencent.supersonic.demo.mapper.builder;

import com.tencent.supersonic.demo.common.enums.DictWordType;
import com.tencent.supersonic.demo.mapper.knowledge.DictWord;
import com.tencent.supersonic.demo.semantic.pojo.SchemaElement;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 维度词构建器
 */
@Component
public class DimensionWordBuilder extends BaseWordBuilder {

    @Override
    public DictWordType getWordType() {
        return DictWordType.DIMENSION;
    }

    @Override
    public List<DictWord> build(List<SchemaElement> elements) {
        List<DictWord> words = new ArrayList<>();

        for (SchemaElement element : elements) {
            if (element == null || StringUtils.isBlank(element.getName())) {
                continue;
            }

            Long modelId = element.getModel();
            Long elementId = element.getId();

            // 添加维度名称
            words.add(DictWord.builder()
                    .word(element.getName().toLowerCase())
                    .nature(buildNature(modelId, elementId))
                    .natureWithFrequency(buildNatureWithFrequency(modelId, elementId))
                    .build());

            // 添加别名
            addAliasWords(words, element);
        }

        return words;
    }
}

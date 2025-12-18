package com.tencent.supersonic.demo.mapper.builder;

import com.tencent.supersonic.demo.common.enums.DictWordType;
import com.tencent.supersonic.demo.mapper.knowledge.DictWord;
import com.tencent.supersonic.demo.semantic.pojo.SchemaElement;
import com.tencent.supersonic.demo.semantic.pojo.SchemaValueMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 维度值词构建器
 */
@Component
public class ValueWordBuilder extends BaseWordBuilder {

    @Override
    public DictWordType getWordType() {
        return DictWordType.VALUE;
    }

    @Override
    public List<DictWord> build(List<SchemaElement> elements) {
        List<DictWord> words = new ArrayList<>();

        for (SchemaElement element : elements) {
            if (element == null || StringUtils.isBlank(element.getName())) {
                continue;
            }

            Long modelId = element.getModel();
            Long dimensionId = element.getId();

            // 添加维度值名称
            words.add(DictWord.builder()
                    .word(element.getName().toLowerCase())
                    .nature(buildValueNature(modelId, dimensionId))
                    .natureWithFrequency(buildValueNatureWithFrequency(modelId, dimensionId))
                    .build());

            // 处理别名
            if (element.getSchemaValueMaps() != null) {
                for (SchemaValueMap valueMap : element.getSchemaValueMaps()) {
                    if (valueMap.getAlias() != null) {
                        for (String alias : valueMap.getAlias()) {
                            if (StringUtils.isNotBlank(alias)) {
                                words.add(DictWord.builder()
                                        .word(alias.toLowerCase())
                                        .nature(buildValueNature(modelId, dimensionId))
                                        .natureWithFrequency(buildValueNatureWithFrequency(modelId, dimensionId))
                                        .alias(valueMap.getTechName())
                                        .build());
                            }
                        }
                    }
                }
            }
        }

        return words;
    }

    /**
     * 构建维度值词性
     */
    private String buildValueNature(Long modelId, Long dimensionId) {
        return DictWordType.NATURE_SPILT + modelId
                + DictWordType.NATURE_SPILT + dimensionId;
    }

    /**
     * 构建带频率的维度值词性
     */
    private String buildValueNatureWithFrequency(Long modelId, Long dimensionId) {
        return buildValueNature(modelId, dimensionId) + DictWordType.SPACE + DEFAULT_FREQUENCY;
    }
}

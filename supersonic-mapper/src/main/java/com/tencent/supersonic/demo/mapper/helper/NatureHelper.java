package com.tencent.supersonic.demo.mapper.helper;

import com.tencent.supersonic.demo.common.enums.DictWordType;
import com.tencent.supersonic.demo.semantic.enums.SchemaElementType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 词性解析辅助类
 */
@Slf4j
public class NatureHelper {

    /**
     * 将词性字符串转换为Schema元素类型
     */
    public static SchemaElementType convertToElementType(String nature) {
        DictWordType dictWordType = DictWordType.getNatureType(nature);
        if (Objects.isNull(dictWordType)) {
            return null;
        }
        SchemaElementType result = null;
        switch (dictWordType) {
            case METRIC:
                result = SchemaElementType.METRIC;
                break;
            case DIMENSION:
                result = SchemaElementType.DIMENSION;
                break;
            case DATASET:
                result = SchemaElementType.DATASET;
                break;
            case VALUE:
                result = SchemaElementType.VALUE;
                break;
            default:
                break;
        }
        return result;
    }

    /**
     * 从词性中获取数据集ID
     */
    public static Long getDataSetId(String nature) {
        return parseIdFromNature(nature, 1);
    }

    /**
     * 从词性中获取模型ID
     */
    private static Long getModelId(String nature) {
        return parseIdFromNature(nature, 1);
    }

    /**
     * 从词性中获取元素ID
     */
    public static Long getElementID(String nature) {
        return parseIdFromNature(nature, 2);
    }

    /**
     * 将模型ID转换为数据集ID的词性
     */
    private static String changeModel2DataSet(String nature, Long dataSetId) {
        try {
            String[] split = nature.split(DictWordType.NATURE_SPILT);
            if (split.length <= 1) {
                return null;
            }
            split[1] = String.valueOf(dataSetId);
            return String.join(DictWordType.NATURE_SPILT, split);
        } catch (NumberFormatException e) {
            log.error("", e);
        }
        return null;
    }

    /**
     * 将模型ID转换为数据集ID列表
     */
    public static List<String> changeModel2DataSet(String nature, Map<Long, List<Long>> modelIdToDataSetIds) {
        Long modelId = getModelId(nature);
        if (modelId == null) {
            return Collections.emptyList();
        }
        List<Long> dataSetIds = modelIdToDataSetIds.get(modelId);
        if (CollectionUtils.isEmpty(dataSetIds)) {
            return Collections.emptyList();
        }
        return dataSetIds.stream()
                .map(dataSetId -> changeModel2DataSet(nature, dataSetId))
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .collect(Collectors.toList());
    }

    /**
     * 从词性中解析ID
     */
    public static Long parseIdFromNature(String nature, int index) {
        if (nature != null && nature.startsWith("_")) {
            try {
                String[] split = nature.split(DictWordType.NATURE_SPILT);
                if (split.length > index) {
                    return Long.valueOf(split[index]);
                }
            } catch (NumberFormatException e) {
                log.error("Error parsing long from nature: {}", nature, e);
            }
        }
        return null;
    }

    /**
     * 判断词性是否有效
     */
    private static boolean isNatureValid(String nature) {
        return StringUtils.isNotEmpty(nature) && nature.startsWith(DictWordType.NATURE_SPILT);
    }
}

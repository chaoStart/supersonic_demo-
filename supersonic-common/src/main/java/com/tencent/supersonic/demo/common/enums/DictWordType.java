package com.tencent.supersonic.demo.common.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * 词性类型枚举
 * 用于标识词的类型：metric、dimension、value等
 */
public enum DictWordType {
    METRIC("metric"),
    DIMENSION("dimension"),
    VALUE("value"),
    DATASET("dataSet"),
    NUMBER("m"),
    SUFFIX("suffix");

    public static final String NATURE_SPILT = "_";
    public static final String SPACE = " ";
    private String type;

    DictWordType(String type) {
        this.type = type;
    }

    public String getType() {
        return NATURE_SPILT + type;
    }

    /**
     * 根据词性字符串获取词性类型
     */
    public static DictWordType getNatureType(String nature) {
        if (StringUtils.isEmpty(nature) || !nature.startsWith(NATURE_SPILT)) {
            return null;
        }
        for (DictWordType dictWordType : values()) {
            if (nature.endsWith(dictWordType.getType())) {
                return dictWordType;
            }
        }
        // dataSet
        String[] natures = nature.split(DictWordType.NATURE_SPILT);
        if (natures.length == 2 && StringUtils.isNumeric(natures[1])) {
            return DATASET;
        }
        // dimension value
        if (natures.length == 3 && StringUtils.isNumeric(natures[1])
                && StringUtils.isNumeric(natures[2])) {
            return VALUE;
        }
        return null;
    }
}

package com.tencent.supersonic.demo.semantic.enums;

/**
 * 维度类型枚举
 */
public enum DimensionType {
    categorical,    // 类目维度
    time,           // 时间维度
    partition_time, // 分区时间维度
    primary_key,    // 主键维度
    foreign_key;    // 外键维度

    public static boolean isPartitionTime(DimensionType dimensionType) {
        return partition_time.equals(dimensionType);
    }

    public static boolean isPrimaryKey(DimensionType dimensionType) {
        return primary_key.equals(dimensionType);
    }
}

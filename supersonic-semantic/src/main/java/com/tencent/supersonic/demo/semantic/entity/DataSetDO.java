package com.tencent.supersonic.demo.semantic.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据集数据对象
 */
@Data
@TableName("s2_data_set")
public class DataSetDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String bizName;
    private String description;
    private Integer status;
    private String alias;           // 别名
    private String modelIds;        // 关联模型ID，逗号分隔
    private String dimensionIds;    // 维度ID列表
    private String metricIds;       // 指标ID列表
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}

package com.tencent.supersonic.demo.semantic.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 维度值数据对象
 */
@Data
@TableName("s2_dimension_value")
public class DimensionValueDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long dimensionId;
    private String value;
    private String alias;
    private Integer frequency;
    private LocalDateTime createdAt;
}

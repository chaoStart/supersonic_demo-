package com.tencent.supersonic.demo.semantic.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 维度数据对象
 */
@Data
@TableName("s2_dimension")
public class DimensionDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long modelId;
    private String name;
    private String bizName;
    private String description;
    private Integer status;
    private String type;          // categorical, time, partition_time
    private String alias;         // 别名，逗号分隔
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}

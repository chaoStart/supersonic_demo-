package com.tencent.supersonic.demo.semantic.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模型数据对象
 */
@Data
@TableName("s2_model")
public class ModelDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String bizName;
    private String description;
    private Integer status;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}

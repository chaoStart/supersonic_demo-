package com.tencent.supersonic.demo.semantic.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Schema值映射
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SchemaValueMap implements Serializable {
    private String techName;    // 技术名称
    private String bizName;     // 业务名称
    private java.util.List<String> alias;  // 别名列表
}

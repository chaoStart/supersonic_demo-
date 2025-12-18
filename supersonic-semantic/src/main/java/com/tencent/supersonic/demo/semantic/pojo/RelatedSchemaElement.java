package com.tencent.supersonic.demo.semantic.pojo;

import com.tencent.supersonic.demo.semantic.enums.SchemaElementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 关联的Schema元素
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RelatedSchemaElement implements Serializable {
    private Long dimensionId;           // 关联维度ID
    private String dimensionBizName;    // 关联维度业务名称
    private boolean necessary;          // 是否必须
}

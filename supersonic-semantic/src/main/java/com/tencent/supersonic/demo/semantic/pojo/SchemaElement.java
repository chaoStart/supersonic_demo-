package com.tencent.supersonic.demo.semantic.pojo;

import com.google.common.base.Objects;
import com.tencent.supersonic.demo.common.pojo.Constants;
import com.tencent.supersonic.demo.semantic.enums.DimensionType;
import com.tencent.supersonic.demo.semantic.enums.SchemaElementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Schema元素 - 语义模型的核心数据结构
 * 表示一个维度、指标或数据集等语义元素
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SchemaElement implements Serializable {

    private Long dataSetId;                 // 数据集ID
    private String dataSetName;             // 数据集名称
    private Long model;                     // 模型ID
    private Long id;                        // 元素ID
    private String name;                    // 元素名称(中文名)
    private String bizName;                 // 业务名称(字段名)
    private Long useCnt;                    // 使用次数
    private SchemaElementType type;         // 元素类型
    private List<String> alias;             // 别名列表
    private List<SchemaValueMap> schemaValueMaps;     // 值映射
    private List<RelatedSchemaElement> relatedSchemaElements;  // 关联元素
    private String defaultAgg;              // 默认聚合函数
    private String dataFormatType;          // 数据格式类型
    private double order;                   // 排序
    private int isTag;                      // 是否标签
    private String description;             // 描述
    @Builder.Default
    private Map<String, Object> extInfo = new HashMap<>();  // 扩展信息
    private DimensionTimeTypeParams typeParams;  // 时间类型参数

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SchemaElement element = (SchemaElement) o;
        return Objects.equal(dataSetId, element.dataSetId)
                && Objects.equal(id, element.id)
                && Objects.equal(name, element.name)
                && Objects.equal(bizName, element.bizName)
                && Objects.equal(type, element.type);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(dataSetId, id, name, bizName, type);
    }

    /**
     * 判断是否为分区时间维度
     */
    public boolean isPartitionTime() {
        if (MapUtils.isEmpty(extInfo)) {
            return false;
        }
        Object o = extInfo.get(Constants.DIMENSION_TYPE);
        DimensionType dimensionType = null;
        if (o instanceof DimensionType) {
            dimensionType = (DimensionType) o;
        }
        if (o instanceof String) {
            dimensionType = DimensionType.valueOf((String) o);
        }
        return DimensionType.isPartitionTime(dimensionType);
    }

    /**
     * 判断是否为主键
     */
    public boolean isPrimaryKey() {
        if (MapUtils.isEmpty(extInfo)) {
            return false;
        }
        Object o = extInfo.get(Constants.DIMENSION_TYPE);
        DimensionType dimensionType = null;
        if (o instanceof DimensionType) {
            dimensionType = (DimensionType) o;
        }
        if (o instanceof String) {
            dimensionType = DimensionType.valueOf((String) o);
        }
        return DimensionType.isPrimaryKey(dimensionType);
    }

    /**
     * 获取时间格式
     */
    public String getTimeFormat() {
        if (MapUtils.isEmpty(extInfo)) {
            return null;
        }
        return (String) extInfo.get(Constants.DIMENSION_TIME_FORMAT);
    }

    /**
     * 获取分区时间格式
     */
    public String getPartitionTimeFormat() {
        String timeFormat = getTimeFormat();
        if (StringUtils.isNotBlank(timeFormat) && isPartitionTime()) {
            return timeFormat;
        }
        return "";
    }
}

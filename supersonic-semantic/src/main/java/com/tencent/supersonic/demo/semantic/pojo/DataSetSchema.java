package com.tencent.supersonic.demo.semantic.pojo;

import com.tencent.supersonic.demo.semantic.enums.SchemaElementType;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 数据集Schema
 * 包含一个数据集的所有维度、指标和维度值信息
 */
@Data
public class DataSetSchema implements Serializable {

    private String databaseType;                      // 数据库类型
    private String databaseVersion;                   // 数据库版本
    private SchemaElement dataSet;                    // 数据集元素
    private Set<SchemaElement> metrics = new HashSet<>();       // 指标集合
    private Set<SchemaElement> dimensions = new HashSet<>();    // 维度集合
    private Set<SchemaElement> dimensionValues = new HashSet<>(); // 维度值集合
    private QueryConfig queryConfig;                  // 查询配置

    /**
     * 获取数据集ID
     */
    public Long getDataSetId() {
        return dataSet.getDataSetId();
    }

    /**
     * 根据元素类型和ID获取Schema元素
     */
    public SchemaElement getElement(SchemaElementType elementType, long elementID) {
        Optional<SchemaElement> element = Optional.empty();

        switch (elementType) {
            case DATASET:
                element = Optional.of(dataSet);
                break;
            case METRIC:
                element = metrics.stream().filter(e -> e.getId() == elementID).findFirst();
                break;
            case DIMENSION:
                element = dimensions.stream().filter(e -> e.getId() == elementID).findFirst();
                break;
            case VALUE:
                element = dimensionValues.stream().filter(e -> e.getId() == elementID).findFirst();
                break;
            default:
        }

        return element.orElse(null);
    }

    /**
     * 获取业务名称到名称的映射
     */
    public Map<String, String> getBizNameToName() {
        List<SchemaElement> allElements = new ArrayList<>();
        allElements.addAll(getDimensions());
        allElements.addAll(getMetrics());
        return allElements.stream().collect(Collectors.toMap(
                SchemaElement::getBizName,
                SchemaElement::getName,
                (k1, k2) -> k1));
    }

    /**
     * 判断是否包含分区时间维度
     */
    public boolean containsPartitionDimensions() {
        return dimensions.stream().anyMatch(SchemaElement::isPartitionTime);
    }

    /**
     * 获取分区时间维度
     */
    public SchemaElement getPartitionDimension() {
        return dimensions.stream()
                .filter(SchemaElement::isPartitionTime)
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取主键维度
     */
    public SchemaElement getPrimaryKey() {
        for (SchemaElement dimension : dimensions) {
            if (dimension.isPrimaryKey()) {
                return dimension;
            }
        }
        return null;
    }

    /**
     * 获取分区时间格式
     */
    public String getPartitionTimeFormat() {
        for (SchemaElement dimension : dimensions) {
            String partitionTimeFormat = dimension.getPartitionTimeFormat();
            if (StringUtils.isNotBlank(partitionTimeFormat)) {
                return partitionTimeFormat;
            }
        }
        return null;
    }
}

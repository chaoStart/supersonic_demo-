package com.tencent.supersonic.demo.semantic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tencent.supersonic.demo.common.pojo.Constants;
import com.tencent.supersonic.demo.semantic.entity.DataSetDO;
import com.tencent.supersonic.demo.semantic.entity.DimensionDO;
import com.tencent.supersonic.demo.semantic.entity.DimensionValueDO;
import com.tencent.supersonic.demo.semantic.entity.MetricDO;
import com.tencent.supersonic.demo.semantic.entity.ModelDO;
import com.tencent.supersonic.demo.semantic.enums.DimensionType;
import com.tencent.supersonic.demo.semantic.enums.SchemaElementType;
import com.tencent.supersonic.demo.semantic.pojo.DataSetSchema;
import com.tencent.supersonic.demo.semantic.pojo.SchemaElement;
import com.tencent.supersonic.demo.semantic.pojo.SchemaValueMap;
import com.tencent.supersonic.demo.semantic.pojo.SemanticSchema;
import com.tencent.supersonic.demo.semantic.repository.DataSetRepository;
import com.tencent.supersonic.demo.semantic.repository.DimensionRepository;
import com.tencent.supersonic.demo.semantic.repository.DimensionValueRepository;
import com.tencent.supersonic.demo.semantic.repository.MetricRepository;
import com.tencent.supersonic.demo.semantic.repository.ModelRepository;
import com.tencent.supersonic.demo.semantic.service.SemanticSchemaService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 语义Schema服务实现
 */
@Slf4j
@Service
public class SemanticSchemaServiceImpl implements SemanticSchemaService {

    @Autowired
    private ModelRepository modelRepository;

    @Autowired
    private DimensionRepository dimensionRepository;

    @Autowired
    private MetricRepository metricRepository;

    @Autowired
    private DataSetRepository dataSetRepository;

    @Autowired
    private DimensionValueRepository dimensionValueRepository;

    private volatile SemanticSchema semanticSchema;
    private volatile Map<Long, List<Long>> modelIdToDataSetIds;
    private volatile boolean initialized = false;

    /**
     * 延迟初始化，确保数据库表已创建和数据已导入
     */
    private synchronized void ensureInitialized() {
        if (!initialized) {
            reload();
            initialized = true;
        }
    }

    @Override
    public SemanticSchema getSemanticSchema() {
        ensureInitialized();
        return semanticSchema;
    }

    @Override
    public DataSetSchema getDataSetSchema(Long dataSetId) {
        ensureInitialized();
        if (semanticSchema != null) {
            return semanticSchema.getDataSetSchema(dataSetId);
        }
        return null;
    }

    @Override
    public Map<Long, List<Long>> getModelIdToDataSetIds() {
        ensureInitialized();
        return modelIdToDataSetIds;
    }

    @Override
    public synchronized void reload() {
        log.info("开始加载语义Schema...");
        initialized = true;  // Set true early to prevent recursive calls

        // 查询所有启用的数据
        List<DataSetDO> dataSets = dataSetRepository.selectList(
                new LambdaQueryWrapper<DataSetDO>().eq(DataSetDO::getStatus, Constants.STATUS_ENABLED));
        List<ModelDO> models = modelRepository.selectList(
                new LambdaQueryWrapper<ModelDO>().eq(ModelDO::getStatus, Constants.STATUS_ENABLED));
        List<DimensionDO> dimensions = dimensionRepository.selectList(
                new LambdaQueryWrapper<DimensionDO>().eq(DimensionDO::getStatus, Constants.STATUS_ENABLED));
        List<MetricDO> metrics = metricRepository.selectList(
                new LambdaQueryWrapper<MetricDO>().eq(MetricDO::getStatus, Constants.STATUS_ENABLED));
        List<DimensionValueDO> dimensionValues = dimensionValueRepository.selectList(null);

        // 构建modelId到dataSetId的映射
        modelIdToDataSetIds = new HashMap<>();
        for (DataSetDO dataSet : dataSets) {
            if (StringUtils.isNotBlank(dataSet.getModelIds())) {
                List<Long> modelIds = Arrays.stream(dataSet.getModelIds().split(","))
                        .map(String::trim)
                        .filter(StringUtils::isNotBlank)
                        .map(Long::parseLong)
                        .collect(Collectors.toList());
                for (Long modelId : modelIds) {
                    modelIdToDataSetIds.computeIfAbsent(modelId, k -> new ArrayList<>()).add(dataSet.getId());
                }
            }
        }

        // 构建维度和指标的Map，按modelId分组
        Map<Long, List<DimensionDO>> modelDimensionMap = dimensions.stream()
                .collect(Collectors.groupingBy(DimensionDO::getModelId));
        Map<Long, List<MetricDO>> modelMetricMap = metrics.stream()
                .collect(Collectors.groupingBy(MetricDO::getModelId));
        Map<Long, List<DimensionValueDO>> dimValueMap = dimensionValues.stream()
                .collect(Collectors.groupingBy(DimensionValueDO::getDimensionId));

        // 构建DataSetSchema列表
        List<DataSetSchema> dataSetSchemaList = new ArrayList<>();
        for (DataSetDO dataSetDO : dataSets) {
            DataSetSchema schema = buildDataSetSchema(dataSetDO, modelDimensionMap, modelMetricMap, dimValueMap);
            dataSetSchemaList.add(schema);
        }

        semanticSchema = new SemanticSchema(dataSetSchemaList);
        log.info("语义Schema加载完成，共{}个数据集", dataSetSchemaList.size());
    }

    private DataSetSchema buildDataSetSchema(DataSetDO dataSetDO,
                                              Map<Long, List<DimensionDO>> modelDimensionMap,
                                              Map<Long, List<MetricDO>> modelMetricMap,
                                              Map<Long, List<DimensionValueDO>> dimValueMap) {
        DataSetSchema schema = new DataSetSchema();

        // 设置数据集元素
        SchemaElement dataSetElement = SchemaElement.builder()
                .id(dataSetDO.getId())
                .dataSetId(dataSetDO.getId())
                .dataSetName(dataSetDO.getName())
                .name(dataSetDO.getName())
                .bizName(dataSetDO.getBizName())
                .type(SchemaElementType.DATASET)
                .alias(parseAlias(dataSetDO.getAlias()))
                .description(dataSetDO.getDescription())
                .build();
        schema.setDataSet(dataSetElement);

        // 获取数据集关联的所有模型ID
        Set<Long> modelIds = new HashSet<>();
        if (StringUtils.isNotBlank(dataSetDO.getModelIds())) {
            modelIds = Arrays.stream(dataSetDO.getModelIds().split(","))
                    .map(String::trim)
                    .filter(StringUtils::isNotBlank)
                    .map(Long::parseLong)
                    .collect(Collectors.toSet());
        }

        // 收集维度
        Set<SchemaElement> dimensionElements = new HashSet<>();
        Set<SchemaElement> dimensionValueElements = new HashSet<>();
        for (Long modelId : modelIds) {
            List<DimensionDO> dims = modelDimensionMap.getOrDefault(modelId, new ArrayList<>());
            for (DimensionDO dim : dims) {
                Map<String, Object> extInfo = new HashMap<>();
                if (StringUtils.isNotBlank(dim.getType())) {
                    try {
                        extInfo.put(Constants.DIMENSION_TYPE, DimensionType.valueOf(dim.getType()));
                    } catch (Exception e) {
                        extInfo.put(Constants.DIMENSION_TYPE, DimensionType.categorical);
                    }
                }

                SchemaElement dimElement = SchemaElement.builder()
                        .id(dim.getId())
                        .dataSetId(dataSetDO.getId())
                        .dataSetName(dataSetDO.getName())
                        .model(modelId)
                        .name(dim.getName())
                        .bizName(dim.getBizName())
                        .type(SchemaElementType.DIMENSION)
                        .alias(parseAlias(dim.getAlias()))
                        .description(dim.getDescription())
                        .extInfo(extInfo)
                        .build();
                dimensionElements.add(dimElement);

                // 添加维度值
                List<DimensionValueDO> values = dimValueMap.getOrDefault(dim.getId(), new ArrayList<>());
                for (DimensionValueDO value : values) {
                    List<SchemaValueMap> valueMaps = new ArrayList<>();
                    valueMaps.add(SchemaValueMap.builder()
                            .techName(value.getValue())
                            .bizName(value.getValue())
                            .alias(parseAlias(value.getAlias()))
                            .build());

                    SchemaElement valueElement = SchemaElement.builder()
                            .id(dim.getId())  // 维度值的ID指向维度ID
                            .dataSetId(dataSetDO.getId())
                            .dataSetName(dataSetDO.getName())
                            .model(modelId)
                            .name(value.getValue())
                            .bizName(value.getValue())
                            .type(SchemaElementType.VALUE)
                            .alias(parseAlias(value.getAlias()))
                            .schemaValueMaps(valueMaps)
                            .build();
                    dimensionValueElements.add(valueElement);
                }
            }
        }
        schema.setDimensions(dimensionElements);
        schema.setDimensionValues(dimensionValueElements);

        // 收集指标
        Set<SchemaElement> metricElements = new HashSet<>();
        for (Long modelId : modelIds) {
            List<MetricDO> mets = modelMetricMap.getOrDefault(modelId, new ArrayList<>());
            for (MetricDO met : mets) {
                SchemaElement metElement = SchemaElement.builder()
                        .id(met.getId())
                        .dataSetId(dataSetDO.getId())
                        .dataSetName(dataSetDO.getName())
                        .model(modelId)
                        .name(met.getName())
                        .bizName(met.getBizName())
                        .type(SchemaElementType.METRIC)
                        .alias(parseAlias(met.getAlias()))
                        .defaultAgg(met.getDefaultAgg())
                        .description(met.getDescription())
                        .build();
                metricElements.add(metElement);
            }
        }
        schema.setMetrics(metricElements);

        return schema;
    }

    private List<String> parseAlias(String alias) {
        if (StringUtils.isBlank(alias)) {
            return new ArrayList<>();
        }
        return Arrays.stream(alias.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
    }
}

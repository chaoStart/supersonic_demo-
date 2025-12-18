package com.tencent.supersonic.demo.app;

import com.tencent.supersonic.demo.mapper.KeywordMapper;
import com.tencent.supersonic.demo.mapper.context.ChatQueryContext;
import com.tencent.supersonic.demo.mapper.context.QueryNLReq;
import com.tencent.supersonic.demo.semantic.enums.MapModeEnum;
import com.tencent.supersonic.demo.semantic.enums.SchemaElementType;
import com.tencent.supersonic.demo.semantic.pojo.SchemaElementMatch;
import com.tencent.supersonic.demo.semantic.pojo.SchemaMapInfo;
import com.tencent.supersonic.demo.semantic.pojo.SemanticSchema;
import com.tencent.supersonic.demo.semantic.service.SemanticSchemaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Mapper字段匹配测试类
 */
@SpringBootTest
public class MapperTest {

    @Autowired
    private KeywordMapper keywordMapper;

    @Autowired
    private SemanticSchemaService semanticSchemaService;

    private SemanticSchema semanticSchema;

    @BeforeEach
    public void setUp() {
        semanticSchema = semanticSchemaService.getSemanticSchema();
        assertNotNull(semanticSchema, "语义Schema不应为空");
    }

    @Test
    @DisplayName("测试指标匹配 - 销售额")
    public void testMetricMapping_SalesAmount() {
        // Given
        String queryText = "查询销售额";
        ChatQueryContext context = createContext(queryText, 1L);

        // When
        keywordMapper.map(context);

        // Then
        SchemaMapInfo mapInfo = context.getMapInfo();
        assertFalse(mapInfo.isEmpty(), "映射结果不应为空");

        List<SchemaElementMatch> matches = mapInfo.getMatchedElements(1L);
        assertTrue(matches.stream()
                .anyMatch(m -> m.getElement().getName().equals("销售额")
                        && m.getElement().getType() == SchemaElementType.METRIC),
                "应该匹配到销售额指标");

        printMatches("指标匹配测试", queryText, matches);
    }

    @Test
    @DisplayName("测试维度匹配 - 商品类别")
    public void testDimensionMapping_ProductCategory() {
        // Given
        String queryText = "按商品类别统计";
        ChatQueryContext context = createContext(queryText, 1L);

        // When
        keywordMapper.map(context);

        // Then
        SchemaMapInfo mapInfo = context.getMapInfo();
        List<SchemaElementMatch> matches = mapInfo.getMatchedElements(1L);

        assertTrue(matches.stream()
                .anyMatch(m -> m.getElement().getName().equals("商品类别")
                        && m.getElement().getType() == SchemaElementType.DIMENSION),
                "应该匹配到商品类别维度");

        printMatches("维度匹配测试", queryText, matches);
    }

    @Test
    @DisplayName("测试别名匹配 - 产品名匹配商品名称")
    public void testAliasMapping() {
        // Given: "产品名"是"商品名称"的别名
        String queryText = "产品名是什么";
        ChatQueryContext context = createContext(queryText, 1L);

        // When
        keywordMapper.map(context);

        // Then
        SchemaMapInfo mapInfo = context.getMapInfo();
        List<SchemaElementMatch> matches = mapInfo.getMatchedElements(1L);

        assertTrue(matches.stream()
                .anyMatch(m -> m.getElement().getName().equals("商品名称")),
                "产品名应该匹配到商品名称（通过别名）");

        printMatches("别名匹配测试", queryText, matches);
    }

    @Test
    @DisplayName("测试多元素匹配 - 北京的用户数")
    public void testMultiElementMapping() {
        // Given
        String queryText = "北京的用户数";
        ChatQueryContext context = createContext(queryText, 1L);

        // When
        keywordMapper.map(context);

        // Then
        SchemaMapInfo mapInfo = context.getMapInfo();
        List<SchemaElementMatch> matches = mapInfo.getMatchedElements(1L);

        // 应该匹配到"用户数"指标
        assertTrue(matches.stream()
                .anyMatch(m -> m.getElement().getName().equals("用户数")
                        && m.getElement().getType() == SchemaElementType.METRIC),
                "应该匹配到用户数指标");

        printMatches("多元素匹配测试", queryText, matches);
    }

    @Test
    @DisplayName("测试指标别名匹配 - 营收匹配销售额")
    public void testMetricAliasMapping() {
        // Given: "营收"是"销售额"的别名
        String queryText = "查询营收情况";
        ChatQueryContext context = createContext(queryText, 1L);

        // When
        keywordMapper.map(context);

        // Then
        SchemaMapInfo mapInfo = context.getMapInfo();
        List<SchemaElementMatch> matches = mapInfo.getMatchedElements(1L);

        assertTrue(matches.stream()
                .anyMatch(m -> m.getElement().getName().equals("销售额")),
                "营收应该匹配到销售额（通过别名）");

        printMatches("指标别名匹配测试", queryText, matches);
    }

    @Test
    @DisplayName("测试复杂查询 - 上海的手机销售额")
    public void testComplexQuery() {
        // Given
        String queryText = "上海的手机销售额";
        ChatQueryContext context = createContext(queryText, 1L);

        // When
        keywordMapper.map(context);

        // Then
        SchemaMapInfo mapInfo = context.getMapInfo();
        List<SchemaElementMatch> matches = mapInfo.getMatchedElements(1L);

        // 应该匹配到销售额
        assertTrue(matches.stream()
                .anyMatch(m -> m.getElement().getName().equals("销售额")),
                "应该匹配到销售额指标");

        printMatches("复杂查询测试", queryText, matches);
    }

    @Test
    @DisplayName("测试Schema加载")
    public void testSchemaLoading() {
        // 验证数据集
        assertFalse(semanticSchema.getDataSets().isEmpty(), "数据集不应为空");

        // 验证维度
        assertFalse(semanticSchema.getDimensions().isEmpty(), "维度不应为空");

        // 验证指标
        assertFalse(semanticSchema.getMetrics().isEmpty(), "指标不应为空");

        System.out.println("=== Schema加载测试 ===");
        System.out.println("数据集数量: " + semanticSchema.getDataSets().size());
        System.out.println("维度数量: " + semanticSchema.getDimensions().size());
        System.out.println("指标数量: " + semanticSchema.getMetrics().size());
        System.out.println("维度值数量: " + semanticSchema.getDimensionValues().size());
    }

    /**
     * 创建查询上下文
     */
    private ChatQueryContext createContext(String queryText, Long dataSetId) {
        Set<Long> dataSetIds = new HashSet<>();
        if (dataSetId != null) {
            dataSetIds.add(dataSetId);
        }

        QueryNLReq request = QueryNLReq.builder()
                .queryText(queryText)
                .dataSetIds(dataSetIds)
                .mapModeEnum(MapModeEnum.STRICT)
                .build();

        ChatQueryContext context = new ChatQueryContext(request);
        context.setSemanticSchema(semanticSchema);
        context.setModelIdToDataSetIds(semanticSchemaService.getModelIdToDataSetIds());

        return context;
    }

    /**
     * 打印匹配结果
     */
    private void printMatches(String testName, String queryText, List<SchemaElementMatch> matches) {
        System.out.println("\n=== " + testName + " ===");
        System.out.println("查询文本: " + queryText);
        System.out.println("匹配结果数: " + matches.size());
        for (SchemaElementMatch match : matches) {
            System.out.printf("  - [%s] %s (bizName=%s, similarity=%.2f, detectWord=%s)%n",
                    match.getElement().getType(),
                    match.getElement().getName(),
                    match.getElement().getBizName(),
                    match.getSimilarity(),
                    match.getDetectWord());
        }
    }
}

# SuperSonic Demo - 字段提取和语义建模演示项目

本项目从 [SuperSonic](https://github.com/tencentmusic/supersonic) 项目中抽取核心的**语义建模**和**字段提取（Mapper映射）**功能，提供独立可运行的演示代码。

## 项目简介

SuperSonic 是一个融合 Chat BI 和 Headless BI 的新一代 BI 平台。本 Demo 项目专注于其中的两个核心功能：

1. **语义建模模块**：管理数据集、维度、指标的定义
2. **Mapper映射模块**：根据用户查询提取语义建模中的字段信息

## 项目结构

```
supersonic_demo/
├── pom.xml                        # Maven父项目
├── README.md                      # 项目说明
├── supersonic-common/             # 通用工具模块
├── supersonic-semantic/           # 语义建模模块
├── supersonic-mapper/             # 字段映射模块
└── supersonic-demo-app/           # 演示应用
```

### 模块说明

| 模块 | 说明 |
|------|------|
| supersonic-common | 通用工具类：DictWordType、ContextUtils、EditDistanceUtils |
| supersonic-semantic | 语义模型：SchemaElement、SemanticSchema、DataSetSchema等 |
| supersonic-mapper | 字段映射：KeywordMapper、KnowledgeBaseService、HanlpHelper等 |
| supersonic-demo-app | Spring Boot演示应用，提供REST API |

## 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+（通过Docker运行）

## 快速开始

### 1. 创建数据库

```bash
# 进入MySQL容器创建数据库
docker exec mysql mysql -u root -p123456 -e "CREATE DATABASE IF NOT EXISTS supersonic_demo DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

### 2. 编译项目

```bash
cd supersonic_demo
mvn clean install -DskipTests
```

### 3. 启动应用

```bash
cd supersonic-demo-app
mvn spring-boot:run
```

或者直接运行：
```bash
java -jar supersonic-demo-app/target/supersonic-demo-app-1.0.0-SNAPSHOT.jar
```

应用启动后，访问 http://localhost:8080

### 4. 运行测试

```bash
mvn test
```

## API接口

### 1. 字段映射接口

**POST** `/api/mapper/map`

请求体：
```json
{
    "queryText": "查询北京的销售额",
    "dataSetIds": [1],
    "mapMode": "STRICT"
}
```

响应：
```json
{
    "queryText": "查询北京的销售额",
    "matchCount": 2,
    "matches": [
        {
            "dataSetId": 1,
            "elementId": 1,
            "elementName": "销售额",
            "elementBizName": "sales_amount",
            "elementType": "METRIC",
            "detectWord": "销售额",
            "matchWord": "销售额",
            "similarity": 1.0
        }
    ]
}
```

### 2. 获取Schema信息

**GET** `/api/mapper/schema?dataSetId=1`

### 3. 重新加载知识库

**POST** `/api/mapper/reload`

## 测试用例

### 测试场景

| 场景 | 输入 | 预期结果 |
|------|------|----------|
| 指标匹配 | "查询销售额" | 匹配到"销售额"指标 |
| 维度匹配 | "按商品类别统计" | 匹配到"商品类别"维度 |
| 别名匹配 | "产品名是什么" | "产品名"匹配到"商品名称"（通过别名） |
| 指标别名 | "查询营收情况" | "营收"匹配到"销售额"（通过别名） |
| 复杂查询 | "上海的手机销售额" | 匹配到"销售额"指标 |

### 使用curl测试

```bash
# 测试字段映射
curl -X POST http://localhost:8080/api/mapper/map \
  -H "Content-Type: application/json" \
  -d '{"queryText":"查询销售额","dataSetIds":[1]}'

# 获取Schema
curl http://localhost:8080/api/mapper/schema?dataSetId=1

# 重新加载知识库
curl -X POST http://localhost:8080/api/mapper/reload
```

## 预置测试数据

项目预置了电商分析场景的测试数据：

### 维度
- 商品名称（别名：产品名、产品名称、商品）
- 商品类别（别名：类别、分类、品类）
- 订单日期（别名：日期、时间、下单日期）
- 用户城市（别名：城市、地区、所在地）
- 用户等级（别名：等级、会员等级、VIP等级）

### 指标
- 销售额（别名：销售金额、销售总额、营收、收入）
- 订单量（别名：订单数、成交量、订单总数）
- 客单价（别名：平均消费、单均、均价）
- 用户数（别名：用户量、人数、客户数）
- 活跃用户数（别名：活跃用户、活跃人数、DAU）

### 维度值
- 商品类别：手机、电脑、服装、家电、食品
- 用户城市：北京、上海、深圳、广州、杭州
- 用户等级：普通会员、VIP会员、黄金会员、钻石会员

## 核心类说明

### 语义建模模块

| 类名 | 说明 |
|------|------|
| SchemaElement | 语义元素（维度/指标/数据集） |
| SchemaElementType | 元素类型枚举 |
| SemanticSchema | 语义Schema容器 |
| DataSetSchema | 数据集Schema |
| SemanticSchemaService | 语义Schema服务 |

### 字段映射模块

| 类名 | 说明 |
|------|------|
| SchemaMapper | 映射器接口 |
| BaseMapper | 基础映射器抽象类 |
| KeywordMapper | 关键词映射器（核心实现） |
| KnowledgeBaseService | 知识库服务（前后缀搜索） |
| HanlpHelper | HanLP分词辅助类 |
| NatureHelper | 词性解析辅助类 |

## 配置说明

`application.yml` 关键配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/supersonic_demo
    username: root
    password: 123456

# Mapper配置
mapper:
  detection:
    size: 8           # 返回结果数
    max-size: 20      # 最大搜索数
  name:
    threshold: 0.3    # 名称相似度阈值
```

## 依赖说明

- **HanLP**: 中文分词和NLP处理
- **MyBatis Plus**: 数据库ORM
- **Spring Boot**: 应用框架
- **Guava**: 工具库

## 注意事项

1. 确保MySQL数据库已启动且可连接
2. 首次启动会自动创建表结构和导入测试数据
3. 修改维度/指标后需调用 `/api/mapper/reload` 重新加载知识库

## License

本项目基于 SuperSonic 项目代码抽取，仅供学习和演示使用。

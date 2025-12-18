-- SuperSonic Demo 测试数据 - 电商分析场景

-- 清空旧数据
DELETE FROM s2_dimension_value;
DELETE FROM s2_data_set;
DELETE FROM s2_metric;
DELETE FROM s2_dimension;
DELETE FROM s2_model;

-- 插入模型数据
INSERT INTO s2_model (id, name, biz_name, description, status, created_by, updated_by) VALUES
(1, '订单模型', 'order_model', '电商订单数据模型', 1, 'admin', 'admin'),
(2, '用户模型', 'user_model', '用户分析数据模型', 1, 'admin', 'admin');

-- 插入维度数据
INSERT INTO s2_dimension (id, model_id, name, biz_name, description, type, alias, status, created_by, updated_by) VALUES
(1, 1, '商品名称', 'product_name', '商品的名称', 'categorical', '产品名,产品名称,商品', 1, 'admin', 'admin'),
(2, 1, '商品类别', 'product_category', '商品所属类别', 'categorical', '类别,分类,品类', 1, 'admin', 'admin'),
(3, 1, '订单日期', 'order_date', '订单创建日期', 'partition_time', '日期,时间,下单日期', 1, 'admin', 'admin'),
(4, 1, '订单状态', 'order_status', '订单当前状态', 'categorical', '状态', 1, 'admin', 'admin'),
(5, 2, '用户名', 'user_name', '用户名称', 'categorical', '姓名,名字,客户名', 1, 'admin', 'admin'),
(6, 2, '用户城市', 'user_city', '用户所在城市', 'categorical', '城市,地区,所在地', 1, 'admin', 'admin'),
(7, 2, '用户等级', 'user_level', '用户会员等级', 'categorical', '等级,会员等级,VIP等级', 1, 'admin', 'admin'),
(8, 2, '注册日期', 'register_date', '用户注册日期', 'time', '注册时间', 1, 'admin', 'admin');

-- 插入指标数据
INSERT INTO s2_metric (id, model_id, name, biz_name, description, type, alias, default_agg, status, created_by, updated_by) VALUES
(1, 1, '销售额', 'sales_amount', '商品销售总金额', 'ATOMIC', '销售金额,销售总额,营收,收入', 'SUM', 1, 'admin', 'admin'),
(2, 1, '订单量', 'order_count', '订单数量', 'ATOMIC', '订单数,成交量,订单总数', 'COUNT', 1, 'admin', 'admin'),
(3, 1, '客单价', 'avg_order_amount', '平均订单金额', 'DERIVED', '平均消费,单均,均价', 'AVG', 1, 'admin', 'admin'),
(4, 1, '退款金额', 'refund_amount', '退款总金额', 'ATOMIC', '退款额,退款', 'SUM', 1, 'admin', 'admin'),
(5, 2, '用户数', 'user_count', '用户总数', 'ATOMIC', '用户量,人数,客户数', 'COUNT', 1, 'admin', 'admin'),
(6, 2, '活跃用户数', 'active_user_count', '活跃用户数量', 'ATOMIC', '活跃用户,活跃人数,DAU', 'COUNT', 1, 'admin', 'admin'),
(7, 2, '新增用户数', 'new_user_count', '新注册用户数', 'ATOMIC', '新用户,新增用户,新客', 'COUNT', 1, 'admin', 'admin'),
(8, 1, '商品数量', 'product_count', '销售的商品数量', 'ATOMIC', '商品数,产品数', 'SUM', 1, 'admin', 'admin');

-- 插入数据集数据
INSERT INTO s2_data_set (id, name, biz_name, description, alias, model_ids, dimension_ids, metric_ids, status, created_by, updated_by) VALUES
(1, '电商综合分析', 'e_commerce_analysis', '电商业务综合分析数据集', '电商分析,销售分析,业务分析', '1,2', '1,2,3,4,5,6,7,8', '1,2,3,4,5,6,7,8', 1, 'admin', 'admin');

-- 插入维度值数据
-- 商品类别
INSERT INTO s2_dimension_value (dimension_id, value, alias, frequency) VALUES
(2, '手机', '智能手机,移动电话,手机数码', 100000),
(2, '电脑', '笔记本,台式机,电脑数码', 100000),
(2, '服装', '衣服,服饰,穿戴', 100000),
(2, '家电', '家用电器,电器', 100000),
(2, '食品', '零食,食物,吃的', 100000);

-- 订单状态
INSERT INTO s2_dimension_value (dimension_id, value, alias, frequency) VALUES
(4, '已完成', '完成,已结束', 100000),
(4, '待发货', '等待发货,未发货', 100000),
(4, '已取消', '取消,已撤销', 100000),
(4, '退款中', '退款,申请退款', 100000);

-- 用户城市
INSERT INTO s2_dimension_value (dimension_id, value, alias, frequency) VALUES
(6, '北京', '首都,BJ,北京市', 100000),
(6, '上海', 'SH,魔都,上海市', 100000),
(6, '深圳', 'SZ,鹏城,深圳市', 100000),
(6, '广州', 'GZ,羊城,广州市', 100000),
(6, '杭州', 'HZ,杭州市', 100000);

-- 用户等级
INSERT INTO s2_dimension_value (dimension_id, value, alias, frequency) VALUES
(7, '普通会员', '普通,普通用户', 100000),
(7, 'VIP会员', 'VIP,贵宾', 100000),
(7, '黄金会员', '黄金,金牌', 100000),
(7, '钻石会员', '钻石,至尊', 100000);

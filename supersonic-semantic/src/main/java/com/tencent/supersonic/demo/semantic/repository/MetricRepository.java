package com.tencent.supersonic.demo.semantic.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tencent.supersonic.demo.semantic.entity.MetricDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 指标Repository
 */
@Mapper
public interface MetricRepository extends BaseMapper<MetricDO> {
}

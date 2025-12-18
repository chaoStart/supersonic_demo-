package com.tencent.supersonic.demo.semantic.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tencent.supersonic.demo.semantic.entity.DimensionValueDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 维度值Repository
 */
@Mapper
public interface DimensionValueRepository extends BaseMapper<DimensionValueDO> {
}

package com.tencent.supersonic.demo.semantic.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tencent.supersonic.demo.semantic.entity.DimensionDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 维度Repository
 */
@Mapper
public interface DimensionRepository extends BaseMapper<DimensionDO> {
}

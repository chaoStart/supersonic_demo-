package com.tencent.supersonic.demo.semantic.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tencent.supersonic.demo.semantic.entity.DataSetDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据集Repository
 */
@Mapper
public interface DataSetRepository extends BaseMapper<DataSetDO> {
}

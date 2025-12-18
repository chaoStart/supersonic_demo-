package com.tencent.supersonic.demo.semantic.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tencent.supersonic.demo.semantic.entity.ModelDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 模型Repository
 */
@Mapper
public interface ModelRepository extends BaseMapper<ModelDO> {
}

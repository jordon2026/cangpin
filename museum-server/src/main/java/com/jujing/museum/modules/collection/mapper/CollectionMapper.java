package com.jujing.museum.modules.collection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jujing.museum.modules.collection.entity.Collection;
import org.apache.ibatis.annotations.Mapper;

/**
 * 藏品Mapper
 */
@Mapper
public interface CollectionMapper extends BaseMapper<Collection> {
}

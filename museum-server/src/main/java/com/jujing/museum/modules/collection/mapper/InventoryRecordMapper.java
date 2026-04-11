package com.jujing.museum.modules.collection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jujing.museum.modules.collection.entity.InventoryRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 盘点记录Mapper
 */
@Mapper
public interface InventoryRecordMapper extends BaseMapper<InventoryRecord> {
}

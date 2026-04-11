package com.jujing.museum.modules.collection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jujing.museum.modules.collection.entity.RepairOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 修复工单Mapper
 */
@Mapper
public interface RepairOrderMapper extends BaseMapper<RepairOrder> {
}

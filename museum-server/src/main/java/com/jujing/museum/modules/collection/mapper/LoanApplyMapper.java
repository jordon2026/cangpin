package com.jujing.museum.modules.collection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jujing.museum.modules.collection.entity.LoanApply;
import org.apache.ibatis.annotations.Mapper;

/**
 * 外借申请Mapper
 */
@Mapper
public interface LoanApplyMapper extends BaseMapper<LoanApply> {
}

package com.jujing.museum.modules.collection.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jujing.museum.common.exception.BizException;
import com.jujing.museum.modules.auth.entity.SysUser;
import com.jujing.museum.modules.auth.mapper.SysUserMapper;
import com.jujing.museum.modules.collection.dto.RepairOrderDTO;
import com.jujing.museum.modules.collection.entity.Collection;
import com.jujing.museum.modules.collection.entity.RepairOrder;
import com.jujing.museum.modules.collection.mapper.CollectionMapper;
import com.jujing.museum.modules.collection.mapper.RepairOrderMapper;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 修复工单服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RepairOrderService {

    private final RepairOrderMapper orderMapper;
    private final CollectionMapper collectionMapper;
    private final SysUserMapper userMapper;

    private static final DateTimeFormatter ORDER_NO_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * 分页查询工单列表
     */
    public IPage<RepairOrder> page(int current, int size, String keyword, Integer status) {
        Page<RepairOrder> page = new Page<>(current, size);
        QueryWrapper<RepairOrder> wrapper = new QueryWrapper<>();
        
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like("order_no", keyword)
                    .or().like("collection_name", keyword)
                    .or().like("collection_no", keyword));
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        
        wrapper.orderByDesc("create_time");
        return orderMapper.selectPage(page, wrapper);
    }

    /**
     * 根据ID查询
     */
    public RepairOrder getById(Long id) {
        return orderMapper.selectById(id);
    }

    /**
     * 创建工单
     */
    @Transactional
    public void add(RepairOrderDTO dto) {
        String orderNo = "XF" + LocalDateTime.now().format(ORDER_NO_FORMAT);
        
        // 获取藏品信息
        Collection collection = null;
        if (dto.getCollectionId() != null) {
            collection = collectionMapper.selectById(dto.getCollectionId());
        }
        
        RepairOrder order = new RepairOrder();
        BeanUtil.copyProperties(dto, order);
        order.setOrderNo(orderNo);
        order.setStatus(0); // 待派单
        
        if (collection != null) {
            order.setCollectionName(collection.getName());
            order.setCollectionNo(collection.getCollectionNo());
        }
        
        // 设置提交人信息
        long userId = StpUtil.getLoginIdAsLong();
        SysUser user = userMapper.selectById(userId);
        if (user != null) {
            order.setSubmitterId(userId);
            order.setSubmitterName(user.getRealName());
        }
        
        orderMapper.insert(order);
        
        // 更新藏品状态为修复中
        if (dto.getCollectionId() != null) {
            collection = collectionMapper.selectById(dto.getCollectionId());
            if (collection != null) {
                collection.setStatus(3); // 修复中
                collectionMapper.updateById(collection);
            }
        }
        
        log.info("创建修复工单: {}", orderNo);
    }

    /**
     * 派单（开始修复）
     */
    @Transactional
    public void dispatch(Long id, Long handlerId) {
        RepairOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BizException("工单不存在");
        }
        if (order.getStatus() != 0) {
            throw new BizException("该工单无法派单");
        }
        
        SysUser handler = userMapper.selectById(handlerId);
        order.setStatus(1); // 修复中
        order.setHandlerId(handlerId);
        order.setHandlerName(handler != null ? handler.getRealName() : "");
        orderMapper.updateById(order);
        
        log.info("派单修复工单: {} -> {}", order.getOrderNo(), handler != null ? handler.getRealName() : "");
    }

    /**
     * 完成修复
     */
    @Transactional
    public void complete(Long id, String repairer, String repairUnit, String repairPlan, java.math.BigDecimal cost) {
        RepairOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BizException("工单不存在");
        }
        if (order.getStatus() != 1) {
            throw new BizException("该工单不在修复中");
        }
        
        order.setStatus(2); // 已完成
        order.setCompleteDate(LocalDateTime.now());
        if (repairer != null) order.setRepairer(repairer);
        if (repairUnit != null) order.setRepairUnit(repairUnit);
        if (repairPlan != null) order.setRepairPlan(repairPlan);
        if (cost != null) order.setCost(cost);
        orderMapper.updateById(order);
        
        // 更新藏品状态为正常
        if (order.getCollectionId() != null) {
            Collection collection = collectionMapper.selectById(order.getCollectionId());
            if (collection != null) {
                collection.setStatus(1); // 正常
                collectionMapper.updateById(collection);
            }
        }
        
        log.info("完成修复工单: {}", order.getOrderNo());
    }

    /**
     * 取消工单
     */
    @Transactional
    public void cancel(Long id) {
        RepairOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BizException("工单不存在");
        }
        if (order.getStatus() == 2) {
            throw new BizException("已完成的工单无法取消");
        }
        
        order.setStatus(3); // 已取消
        orderMapper.updateById(order);
        
        // 恢复藏品状态
        if (order.getCollectionId() != null) {
            Collection collection = collectionMapper.selectById(order.getCollectionId());
            if (collection != null) {
                collection.setStatus(1); // 正常
                collectionMapper.updateById(collection);
            }
        }
        
        log.info("取消修复工单: {}", order.getOrderNo());
    }

    /**
     * 删除工单
     */
    @Transactional
    public void delete(Long id) {
        RepairOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BizException("工单不存在");
        }
        if (order.getStatus() == 1) {
            throw new BizException("修复中的工单无法删除");
        }
        
        orderMapper.deleteById(id);
        log.info("删除修复工单: {}", order.getOrderNo());
    }
}

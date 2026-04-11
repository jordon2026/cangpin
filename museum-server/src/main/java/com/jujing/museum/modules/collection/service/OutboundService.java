package com.jujing.museum.modules.collection.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jujing.museum.common.exception.BizException;
import com.jujing.museum.modules.collection.dto.OutboundDTO;
import com.jujing.museum.modules.collection.entity.Outbound;
import com.jujing.museum.modules.collection.entity.Collection;
import com.jujing.museum.modules.collection.mapper.OutboundMapper;
import com.jujing.museum.modules.collection.mapper.CollectionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 出库服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OutboundService {

    private final OutboundMapper outboundMapper;
    private final CollectionMapper collectionMapper;

    public IPage<OutboundDTO> pageList(int current, int size, String keyword, Integer status) {
        LambdaQueryWrapper<Outbound> wrapper = new LambdaQueryWrapper<>();
        
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Outbound::getOutboundNo, keyword)
                    .or().like(Outbound::getCollectionName, keyword));
        }
        if (status != null) {
            wrapper.eq(Outbound::getStatus, status);
        }
        
        wrapper.orderByDesc(Outbound::getCreateTime);
        
        Page<Outbound> page = new Page<>(current, size);
        IPage<Outbound> result = outboundMapper.selectPage(page, wrapper);
        
        return result.convert(this::toDTO);
    }

    public OutboundDTO getById(Long id) {
        Outbound outbound = outboundMapper.selectById(id);
        if (outbound == null) {
            throw new BizException("出库记录不存在");
        }
        return toDTO(outbound);
    }

    @Transactional
    public void add(OutboundDTO dto) {
        String outboundNo = generateOutboundNo();
        
        Outbound outbound = new Outbound();
        outbound.setOutboundNo(outboundNo);
        outbound.setCollectionId(dto.getCollectionId());
        outbound.setCollectionName(dto.getCollectionName());
        outbound.setCollectionNo(dto.getCollectionNo());
        outbound.setQuantity(dto.getQuantity() != null ? dto.getQuantity() : 1);
        outbound.setDestination(dto.getDestination());
        outbound.setReason(dto.getReason());
        outbound.setStorageId(dto.getStorageId());
        outbound.setHandler(dto.getHandler());
        outbound.setOutboundDate(dto.getOutboundDate() != null ? dto.getOutboundDate() : LocalDate.now());
        outbound.setExpectedReturnDate(dto.getExpectedReturnDate());
        outbound.setStatus(0);
        outbound.setRemark(dto.getRemark());
        outbound.setApplicantId(StpUtil.getLoginIdAsLong());
        outbound.setCreateTime(LocalDateTime.now());
        
        outboundMapper.insert(outbound);
        log.info("新增出库申请: {}", outboundNo);
    }

    @Transactional
    public void approve(Long id, boolean approved, String remark) {
        Outbound outbound = outboundMapper.selectById(id);
        if (outbound == null) {
            throw new BizException("出库记录不存在");
        }
        
        if (approved) {
            outbound.setStatus(1);
            // 更新藏品状态为借出
            if (outbound.getCollectionId() != null) {
                Collection collection = collectionMapper.selectById(outbound.getCollectionId());
                if (collection != null) {
                    collection.setStatus(2);
                    collectionMapper.updateById(collection);
                }
            }
        } else {
            outbound.setStatus(2);
        }
        
        if (StrUtil.isNotBlank(remark)) {
            outbound.setRemark(remark);
        }
        outbound.setUpdateTime(LocalDateTime.now());
        outboundMapper.updateById(outbound);
        
        log.info("{}出库申请: {}", approved ? "批准" : "拒绝", outbound.getOutboundNo());
    }

    @Transactional
    public void return_(Long id) {
        Outbound outbound = outboundMapper.selectById(id);
        if (outbound == null) {
            throw new BizException("出库记录不存在");
        }
        if (outbound.getStatus() != 1) {
            throw new BizException("只能归还已出库的记录");
        }
        
        outbound.setStatus(3);
        outbound.setUpdateTime(LocalDateTime.now());
        outboundMapper.updateById(outbound);
        
        // 更新藏品状态为正常
        if (outbound.getCollectionId() != null) {
            Collection collection = collectionMapper.selectById(outbound.getCollectionId());
            if (collection != null) {
                collection.setStatus(1);
                collectionMapper.updateById(collection);
            }
        }
        
        log.info("藏品已归还: {}", outbound.getOutboundNo());
    }

    public void delete(Long id) {
        Outbound outbound = outboundMapper.selectById(id);
        if (outbound == null) {
            throw new BizException("出库记录不存在");
        }
        if (outbound.getStatus() != 0) {
            throw new BizException("只能删除待审核的记录");
        }
        outboundMapper.deleteById(id);
    }

    private OutboundDTO toDTO(Outbound outbound) {
        OutboundDTO dto = new OutboundDTO();
        dto.setId(outbound.getId());
        dto.setOutboundNo(outbound.getOutboundNo());
        dto.setCollectionId(outbound.getCollectionId());
        dto.setCollectionName(outbound.getCollectionName());
        dto.setCollectionNo(outbound.getCollectionNo());
        dto.setQuantity(outbound.getQuantity());
        dto.setDestination(outbound.getDestination());
        dto.setReason(outbound.getReason());
        dto.setStorageId(outbound.getStorageId());
        dto.setHandler(outbound.getHandler());
        dto.setOutboundDate(outbound.getOutboundDate());
        dto.setExpectedReturnDate(outbound.getExpectedReturnDate());
        dto.setStatus(outbound.getStatus());
        dto.setRemark(outbound.getRemark());
        dto.setCreateTime(outbound.getCreateTime());
        return dto;
    }

    private String generateOutboundNo() {
        String prefix = "CK";
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = outboundMapper.selectCount(null) + 1;
        return prefix + date + String.format("%04d", count);
    }
}

package com.jujing.museum.modules.collection.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jujing.museum.common.exception.BizException;
import com.jujing.museum.modules.collection.dto.InboundDTO;
import com.jujing.museum.modules.collection.entity.Inbound;
import com.jujing.museum.modules.collection.entity.Collection;
import com.jujing.museum.modules.collection.mapper.InboundMapper;
import com.jujing.museum.modules.collection.mapper.CollectionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 入库服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InboundService {

    private final InboundMapper inboundMapper;
    private final CollectionMapper collectionMapper;

    public IPage<InboundDTO> pageList(int current, int size, String keyword, Integer status) {
        LambdaQueryWrapper<Inbound> wrapper = new LambdaQueryWrapper<>();
        
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Inbound::getInboundNo, keyword)
                    .or().like(Inbound::getCollectionName, keyword));
        }
        if (status != null) {
            wrapper.eq(Inbound::getStatus, status);
        }
        
        wrapper.orderByDesc(Inbound::getCreateTime);
        
        Page<Inbound> page = new Page<>(current, size);
        IPage<Inbound> result = inboundMapper.selectPage(page, wrapper);
        
        return result.convert(this::toDTO);
    }

    public InboundDTO getById(Long id) {
        Inbound inbound = inboundMapper.selectById(id);
        if (inbound == null) {
            throw new BizException("入库记录不存在");
        }
        return toDTO(inbound);
    }

    @Transactional
    public void add(InboundDTO dto) {
        String inboundNo = generateInboundNo();
        
        Inbound inbound = new Inbound();
        inbound.setInboundNo(inboundNo);
        inbound.setCollectionId(dto.getCollectionId());
        inbound.setCollectionName(dto.getCollectionName());
        inbound.setCollectionNo(dto.getCollectionNo());
        inbound.setQuantity(dto.getQuantity() != null ? dto.getQuantity() : 1);
        inbound.setSource(dto.getSource());
        inbound.setStorageId(dto.getStorageId());
        inbound.setHandler(dto.getHandler());
        inbound.setInboundDate(dto.getInboundDate() != null ? dto.getInboundDate() : LocalDate.now());
        inbound.setStatus(0);
        inbound.setRemark(dto.getRemark());
        inbound.setApplicantId(StpUtil.getLoginIdAsLong());
        inbound.setCreateTime(LocalDateTime.now());
        
        inboundMapper.insert(inbound);
        log.info("新增入库申请: {}", inboundNo);
    }

    @Transactional
    public void approve(Long id, boolean approved, String remark) {
        Inbound inbound = inboundMapper.selectById(id);
        if (inbound == null) {
            throw new BizException("入库记录不存在");
        }
        
        if (approved) {
            inbound.setStatus(1);
            // 更新藏品状态和库房
            if (inbound.getCollectionId() != null) {
                Collection collection = collectionMapper.selectById(inbound.getCollectionId());
                if (collection != null) {
                    collection.setStatus(1);
                    collection.setStorageId(inbound.getStorageId());
                    collectionMapper.updateById(collection);
                }
            }
        } else {
            inbound.setStatus(2);
        }
        
        if (StrUtil.isNotBlank(remark)) {
            inbound.setRemark(remark);
        }
        inbound.setUpdateTime(LocalDateTime.now());
        inboundMapper.updateById(inbound);
        
        log.info("{}入库申请: {}", approved ? "批准" : "拒绝", inbound.getInboundNo());
    }

    public void delete(Long id) {
        Inbound inbound = inboundMapper.selectById(id);
        if (inbound == null) {
            throw new BizException("入库记录不存在");
        }
        if (inbound.getStatus() == 1) {
            throw new BizException("已入库的记录不能删除");
        }
        inboundMapper.deleteById(id);
    }

    private InboundDTO toDTO(Inbound inbound) {
        InboundDTO dto = new InboundDTO();
        dto.setId(inbound.getId());
        dto.setInboundNo(inbound.getInboundNo());
        dto.setCollectionId(inbound.getCollectionId());
        dto.setCollectionName(inbound.getCollectionName());
        dto.setCollectionNo(inbound.getCollectionNo());
        dto.setQuantity(inbound.getQuantity());
        dto.setSource(inbound.getSource());
        dto.setStorageId(inbound.getStorageId());
        dto.setHandler(inbound.getHandler());
        dto.setInboundDate(inbound.getInboundDate());
        dto.setStatus(inbound.getStatus());
        dto.setRemark(inbound.getRemark());
        dto.setCreateTime(inbound.getCreateTime());
        return dto;
    }

    private String generateInboundNo() {
        String prefix = "RK";
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = inboundMapper.selectCount(null) + 1;
        return prefix + date + String.format("%04d", count);
    }
}

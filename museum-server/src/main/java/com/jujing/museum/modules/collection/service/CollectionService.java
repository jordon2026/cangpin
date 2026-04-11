package com.jujing.museum.modules.collection.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jujing.museum.common.exception.BizException;

import com.jujing.museum.modules.collection.dto.CollectionDTO;
import com.jujing.museum.modules.collection.entity.Collection;
import com.jujing.museum.modules.collection.mapper.CollectionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 藏品服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CollectionService {

    private final CollectionMapper collectionMapper;

    /**
     * 分页查询藏品列表
     */
    public IPage<CollectionDTO> pageList(int current, int size, String keyword, String category, Integer status) {
        LambdaQueryWrapper<Collection> wrapper = new LambdaQueryWrapper<>();
        
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Collection::getName, keyword)
                    .or().like(Collection::getCollectionNo, keyword));
        }
        if (StrUtil.isNotBlank(category)) {
            wrapper.eq(Collection::getCategory, category);
        }
        if (status != null) {
            wrapper.eq(Collection::getStatus, status);
        }
        
        wrapper.orderByDesc(Collection::getCreateTime);
        
        Page<Collection> page = new Page<>(current, size);
        IPage<Collection> result = collectionMapper.selectPage(page, wrapper);
        
        // 转换为DTO
        return result.convert(this::toDTO);
    }

    /**
     * 获取藏品详情
     */
    public CollectionDTO getById(Long id) {
        Collection collection = collectionMapper.selectById(id);
        if (collection == null) {
            throw new BizException("藏品不存在");
        }
        return toDTO(collection);
    }

    /**
     * 新增藏品
     */
    public void add(CollectionDTO dto) {
        // 检查藏品编号是否重复
        LambdaQueryWrapper<Collection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Collection::getCollectionNo, dto.getCollectionNo());
        if (collectionMapper.selectCount(wrapper) > 0) {
            throw new BizException("藏品编号已存在");
        }
        
        Collection collection = new Collection();
        collection.setCollectionNo(dto.getCollectionNo());
        collection.setName(dto.getName());
        collection.setCategory(dto.getCategory());
        collection.setEra(dto.getEra());
        collection.setMaterial(dto.getMaterial());
        collection.setDimensions(dto.getDimensions());
        collection.setWeight(dto.getWeight());
        collection.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        collection.setStorageId(dto.getStorageId());
        collection.setDescription(dto.getDescription());
        collection.setImageUrl(dto.getImageUrl());
        collection.setRemark(dto.getRemark());
        collection.setStatus2(1);
        collection.setCreatorId(StpUtil.getLoginIdAsLong());
        collection.setCreateTime(LocalDateTime.now());
        
        collectionMapper.insert(collection);
        log.info("新增藏品: {}", dto.getName());
    }

    /**
     * 更新藏品
     */
    public void update(CollectionDTO dto) {
        Collection collection = collectionMapper.selectById(dto.getId());
        if (collection == null) {
            throw new BizException("藏品不存在");
        }
        
        // 检查藏品编号是否重复（排除自己）
        if (StrUtil.isNotBlank(dto.getCollectionNo())) {
            LambdaQueryWrapper<Collection> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Collection::getCollectionNo, dto.getCollectionNo())
                   .ne(Collection::getId, dto.getId());
            if (collectionMapper.selectCount(wrapper) > 0) {
                throw new BizException("藏品编号已存在");
            }
            collection.setCollectionNo(dto.getCollectionNo());
        }
        
        if (dto.getName() != null) collection.setName(dto.getName());
        if (dto.getCategory() != null) collection.setCategory(dto.getCategory());
        if (dto.getEra() != null) collection.setEra(dto.getEra());
        if (dto.getMaterial() != null) collection.setMaterial(dto.getMaterial());
        if (dto.getDimensions() != null) collection.setDimensions(dto.getDimensions());
        if (dto.getWeight() != null) collection.setWeight(dto.getWeight());
        if (dto.getStatus() != null) collection.setStatus(dto.getStatus());
        if (dto.getStorageId() != null) collection.setStorageId(dto.getStorageId());
        if (dto.getDescription() != null) collection.setDescription(dto.getDescription());
        if (dto.getImageUrl() != null) collection.setImageUrl(dto.getImageUrl());
        if (dto.getRemark() != null) collection.setRemark(dto.getRemark());
        collection.setUpdateTime(LocalDateTime.now());
        
        collectionMapper.updateById(collection);
        log.info("更新藏品: {}", dto.getName());
    }

    /**
     * 删除藏品
     */
    public void delete(Long id) {
        Collection collection = collectionMapper.selectById(id);
        if (collection == null) {
            throw new BizException("藏品不存在");
        }
        collectionMapper.deleteById(id);
        log.info("删除藏品: {}", collection.getName());
    }

    /**
     * 批量删除
     */
    public void batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BizException("请选择要删除的藏品");
        }
        collectionMapper.deleteBatchIds(ids);
        log.info("批量删除藏品: {}", ids);
    }

    /**
     * 获取所有分类
     */
    public List<String> getAllCategories() {
        LambdaQueryWrapper<Collection> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(Collection::getCategory)
               .isNotNull(Collection::getCategory)
               .ne(Collection::getCategory, "")
               .groupBy(Collection::getCategory);
        List<Collection> list = collectionMapper.selectList(wrapper);
        return list.stream()
                .map(Collection::getCategory)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 转换为DTO
     */
    private CollectionDTO toDTO(Collection collection) {
        CollectionDTO dto = new CollectionDTO();
        dto.setId(collection.getId());
        dto.setCollectionNo(collection.getCollectionNo());
        dto.setName(collection.getName());
        dto.setCategory(collection.getCategory());
        dto.setEra(collection.getEra());
        dto.setMaterial(collection.getMaterial());
        dto.setDimensions(collection.getDimensions());
        dto.setWeight(collection.getWeight());
        dto.setStatus(collection.getStatus());
        dto.setStorageId(collection.getStorageId());
        dto.setDescription(collection.getDescription());
        dto.setImageUrl(collection.getImageUrl());
        dto.setRemark(collection.getRemark());
        dto.setCreateTime(collection.getCreateTime());
        return dto;
    }
}

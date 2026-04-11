package com.jujing.museum.modules.collection.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jujing.museum.common.exception.BizException;
import com.jujing.museum.modules.collection.dto.StorageDTO;
import com.jujing.museum.modules.collection.entity.Storage;
import com.jujing.museum.modules.collection.mapper.StorageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 库房服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {

    private final StorageMapper storageMapper;

    /**
     * 分页查询库房列表
     */
    public IPage<StorageDTO> pageList(int current, int size, String keyword) {
        LambdaQueryWrapper<Storage> wrapper = new LambdaQueryWrapper<>();
        
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Storage::getName, keyword)
                    .or().like(Storage::getStorageNo, keyword));
        }
        
        wrapper.orderByDesc(Storage::getCreateTime);
        
        Page<Storage> page = new Page<>(current, size);
        IPage<Storage> result = storageMapper.selectPage(page, wrapper);
        
        return result.convert(this::toDTO);
    }

    /**
     * 获取所有库房
     */
    public List<StorageDTO> getAll() {
        LambdaQueryWrapper<Storage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Storage::getStatus, 1).orderByAsc(Storage::getName);
        List<Storage> list = storageMapper.selectList(wrapper);
        return list.stream().map(this::toDTO).toList();
    }

    /**
     * 获取库房详情
     */
    public StorageDTO getById(Long id) {
        Storage storage = storageMapper.selectById(id);
        if (storage == null) {
            throw new BizException("库房不存在");
        }
        return toDTO(storage);
    }

    /**
     * 新增库房
     */
    public void add(StorageDTO dto) {
        // 检查编号是否重复
        LambdaQueryWrapper<Storage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Storage::getStorageNo, dto.getStorageNo());
        if (storageMapper.selectCount(wrapper) > 0) {
            throw new BizException("库房编号已存在");
        }
        
        Storage storage = new Storage();
        storage.setStorageNo(dto.getStorageNo());
        storage.setName(dto.getName());
        storage.setLocation(dto.getLocation());
        storage.setCapacity(dto.getCapacity());
        storage.setCurrentCount(0);
        storage.setManager(dto.getManager());
        storage.setPhone(dto.getPhone());
        storage.setTemperature(dto.getTemperature());
        storage.setHumidity(dto.getHumidity());
        storage.setStatus(1);
        storage.setRemark(dto.getRemark());
        storage.setCreateTime(LocalDateTime.now());
        
        storageMapper.insert(storage);
        log.info("新增库房: {}", dto.getName());
    }

    /**
     * 更新库房
     */
    public void update(StorageDTO dto) {
        Storage storage = storageMapper.selectById(dto.getId());
        if (storage == null) {
            throw new BizException("库房不存在");
        }
        
        if (dto.getName() != null) storage.setName(dto.getName());
        if (dto.getLocation() != null) storage.setLocation(dto.getLocation());
        if (dto.getCapacity() != null) storage.setCapacity(dto.getCapacity());
        if (dto.getManager() != null) storage.setManager(dto.getManager());
        if (dto.getPhone() != null) storage.setPhone(dto.getPhone());
        if (dto.getTemperature() != null) storage.setTemperature(dto.getTemperature());
        if (dto.getHumidity() != null) storage.setHumidity(dto.getHumidity());
        if (dto.getStatus() != null) storage.setStatus(dto.getStatus());
        if (dto.getRemark() != null) storage.setRemark(dto.getRemark());
        storage.setUpdateTime(LocalDateTime.now());
        
        storageMapper.updateById(storage);
        log.info("更新库房: {}", dto.getName());
    }

    /**
     * 删除库房
     */
    public void delete(Long id) {
        Storage storage = storageMapper.selectById(id);
        if (storage == null) {
            throw new BizException("库房不存在");
        }
        storageMapper.deleteById(id);
        log.info("删除库房: {}", storage.getName());
    }

    /**
     * 转换为DTO
     */
    private StorageDTO toDTO(Storage storage) {
        StorageDTO dto = new StorageDTO();
        dto.setId(storage.getId());
        dto.setStorageNo(storage.getStorageNo());
        dto.setName(storage.getName());
        dto.setLocation(storage.getLocation());
        dto.setCapacity(storage.getCapacity());
        dto.setCurrentCount(storage.getCurrentCount());
        dto.setManager(storage.getManager());
        dto.setPhone(storage.getPhone());
        dto.setTemperature(storage.getTemperature());
        dto.setHumidity(storage.getHumidity());
        dto.setStatus(storage.getStatus());
        dto.setRemark(storage.getRemark());
        dto.setCreateTime(storage.getCreateTime());
        return dto;
    }
}

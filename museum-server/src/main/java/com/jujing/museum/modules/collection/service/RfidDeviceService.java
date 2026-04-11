package com.jujing.museum.modules.collection.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jujing.museum.common.exception.BizException;
import com.jujing.museum.modules.collection.dto.RfidDeviceDTO;
import com.jujing.museum.modules.collection.entity.RfidDevice;
import com.jujing.museum.modules.collection.mapper.RfidDeviceMapper;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * RFID设备服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RfidDeviceService {

    private final RfidDeviceMapper deviceMapper;

    /**
     * 分页查询设备列表
     */
    public IPage<RfidDevice> page(int current, int size, String keyword, Integer status) {
        Page<RfidDevice> page = new Page<>(current, size);
        QueryWrapper<RfidDevice> wrapper = new QueryWrapper<>();
        
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like("name", keyword)
                    .or().like("device_no", keyword)
                    .or().like("ip", keyword));
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        
        wrapper.orderByDesc("create_time");
        return deviceMapper.selectPage(page, wrapper);
    }

    /**
     * 根据ID查询
     */
    public RfidDevice getById(Long id) {
        return deviceMapper.selectById(id);
    }

    /**
     * 新增设备
     */
    @Transactional
    public void add(RfidDeviceDTO dto) {
        // 检查设备编号是否已存在
        QueryWrapper<RfidDevice> wrapper = new QueryWrapper<>();
        wrapper.eq("device_no", dto.getDeviceNo());
        if (deviceMapper.selectCount(wrapper) > 0) {
            throw new BizException("设备编号已存在");
        }
        
        RfidDevice device = new RfidDevice();
        BeanUtil.copyProperties(dto, device);
        device.setStatus(0); // 默认离线
        deviceMapper.insert(device);
        log.info("新增RFID设备: {}", dto.getName());
    }

    /**
     * 更新设备
     */
    @Transactional
    public void update(RfidDeviceDTO dto) {
        RfidDevice device = deviceMapper.selectById(dto.getId());
        if (device == null) {
            throw new BizException("设备不存在");
        }
        
        BeanUtil.copyProperties(dto, device, "id", "createTime");
        deviceMapper.updateById(device);
        log.info("更新RFID设备: {}", dto.getName());
    }

    /**
     * 删除设备
     */
    @Transactional
    public void delete(Long id) {
        RfidDevice device = deviceMapper.selectById(id);
        if (device == null) {
            throw new BizException("设备不存在");
        }
        
        deviceMapper.deleteById(id);
        log.info("删除RFID设备: {}", device.getName());
    }

    /**
     * 切换设备状态
     */
    @Transactional
    public void toggleStatus(Long id, Integer status) {
        RfidDevice device = deviceMapper.selectById(id);
        if (device == null) {
            throw new BizException("设备不存在");
        }
        
        device.setStatus(status);
        deviceMapper.updateById(device);
        log.info("更新RFID设备状态: {} -> {}", device.getName(), status);
    }
}

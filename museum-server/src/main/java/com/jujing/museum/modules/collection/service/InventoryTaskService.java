package com.jujing.museum.modules.collection.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jujing.museum.common.exception.BizException;
import com.jujing.museum.modules.auth.entity.SysUser;
import com.jujing.museum.modules.auth.mapper.SysUserMapper;
import com.jujing.museum.modules.collection.dto.InventoryTaskDTO;
import com.jujing.museum.modules.collection.entity.Collection;
import com.jujing.museum.modules.collection.entity.InventoryRecord;
import com.jujing.museum.modules.collection.entity.InventoryTask;
import com.jujing.museum.modules.collection.mapper.CollectionMapper;
import com.jujing.museum.modules.collection.mapper.InventoryRecordMapper;
import com.jujing.museum.modules.collection.mapper.InventoryTaskMapper;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 盘点任务服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryTaskService {

    private final InventoryTaskMapper taskMapper;
    private final InventoryRecordMapper recordMapper;
    private final CollectionMapper collectionMapper;
    private final SysUserMapper userMapper;

    private static final DateTimeFormatter TASK_NO_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * 分页查询任务列表
     */
    public IPage<InventoryTask> page(int current, int size, String keyword, Integer status) {
        Page<InventoryTask> page = new Page<>(current, size);
        QueryWrapper<InventoryTask> wrapper = new QueryWrapper<>();
        
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like("task_no", keyword)
                    .or().like("name", keyword)
                    .or().like("storage_name", keyword));
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        
        wrapper.orderByDesc("create_time");
        return taskMapper.selectPage(page, wrapper);
    }

    /**
     * 根据ID查询
     */
    public InventoryTask getById(Long id) {
        return taskMapper.selectById(id);
    }

    /**
     * 创建盘点任务
     */
    @Transactional
    public void add(InventoryTaskDTO dto) {
        // 生成任务编号
        String taskNo = "PD" + LocalDateTime.now().format(TASK_NO_FORMAT);
        
        InventoryTask task = new InventoryTask();
        BeanUtil.copyProperties(dto, task);
        task.setTaskNo(taskNo);
        task.setStatus(0); // 待盘点
        task.setPlanCount(0);
        task.setActualCount(0);
        
        // 设置负责人信息
        if (dto.getHandlerId() != null) {
            SysUser user = userMapper.selectById(dto.getHandlerId());
            if (user != null) {
                task.setHandlerName(user.getRealName());
            }
        }
        
        taskMapper.insert(task);
        log.info("创建盘点任务: {}", taskNo);
    }

    /**
     * 开始盘点
     */
    @Transactional
    public void start(Long id) {
        InventoryTask task = taskMapper.selectById(id);
        if (task == null) {
            throw new BizException("任务不存在");
        }
        if (task.getStatus() != 0) {
            throw new BizException("该任务无法开始盘点");
        }
        
        // 查询库房中的所有藏品，生成盘点记录
        QueryWrapper<Collection> wrapper = new QueryWrapper<>();
        wrapper.eq("storage_id", task.getStorageId());
        List<Collection> collections = collectionMapper.selectList(wrapper);
        
        long userId = StpUtil.getLoginIdAsLong();
        SysUser user = userMapper.selectById(userId);
        
        for (Collection collection : collections) {
            InventoryRecord record = new InventoryRecord();
            record.setTaskId(task.getId());
            record.setTaskNo(task.getTaskNo());
            record.setCollectionId(collection.getId());
            record.setCollectionName(collection.getName());
            record.setCollectionNo(collection.getCollectionNo());
            record.setStatus(0); // 未盘点
            record.setOperatorId(userId);
            record.setOperatorName(user != null ? user.getRealName() : "");
            recordMapper.insert(record);
        }
        
        task.setStatus(1); // 盘点中
        task.setStartTime(LocalDateTime.now());
        task.setPlanCount(collections.size());
        taskMapper.updateById(task);
        
        log.info("开始盘点任务: {}", task.getTaskNo());
    }

    /**
     * 完成盘点
     */
    @Transactional
    public void complete(Long id) {
        InventoryTask task = taskMapper.selectById(id);
        if (task == null) {
            throw new BizException("任务不存在");
        }
        if (task.getStatus() != 1) {
            throw new BizException("该任务不在盘点中");
        }
        
        // 统计盘点结果
        QueryWrapper<InventoryRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("task_id", id);
        List<InventoryRecord> records = recordMapper.selectList(wrapper);
        
        long actualCount = records.stream().filter(r -> r.getStatus() == 1).count();
        long diffCount = records.stream().filter(r -> r.getStatus() == 2).count();
        
        task.setStatus(2); // 已完成
        task.setEndTime(LocalDateTime.now());
        task.setActualCount((int) actualCount);
        task.setDiffCount((int) diffCount);
        task.setResult(diffCount > 0 ? 1 : 0);
        taskMapper.updateById(task);
        
        log.info("完成盘点任务: {}, 盘点数量: {}, 差异数量: {}", task.getTaskNo(), actualCount, diffCount);
    }

    /**
     * 取消任务
     */
    @Transactional
    public void cancel(Long id) {
        InventoryTask task = taskMapper.selectById(id);
        if (task == null) {
            throw new BizException("任务不存在");
        }
        if (task.getStatus() == 2) {
            throw new BizException("已完成的任务无法取消");
        }
        
        task.setStatus(3); // 已取消
        taskMapper.updateById(task);
        
        // 删除关联的盘点记录
        QueryWrapper<InventoryRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("task_id", id);
        recordMapper.delete(wrapper);
        
        log.info("取消盘点任务: {}", task.getTaskNo());
    }

    /**
     * 删除任务
     */
    @Transactional
    public void delete(Long id) {
        InventoryTask task = taskMapper.selectById(id);
        if (task == null) {
            throw new BizException("任务不存在");
        }
        if (task.getStatus() == 1) {
            throw new BizException("盘点中的任务无法删除");
        }
        
        // 删除关联的盘点记录
        QueryWrapper<InventoryRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("task_id", id);
        recordMapper.delete(wrapper);
        
        taskMapper.deleteById(id);
        log.info("删除盘点任务: {}", task.getTaskNo());
    }

    /**
     * 获取任务关联的盘点记录
     */
    public IPage<InventoryRecord> getRecords(Long taskId, int current, int size) {
        Page<InventoryRecord> page = new Page<>(current, size);
        QueryWrapper<InventoryRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("task_id", taskId).orderByDesc("create_time");
        return recordMapper.selectPage(page, wrapper);
    }

    /**
     * 更新盘点记录状态
     */
    @Transactional
    public void updateRecordStatus(Long recordId, Integer status) {
        InventoryRecord record = recordMapper.selectById(recordId);
        if (record == null) {
            throw new BizException("记录不存在");
        }
        
        record.setStatus(status);
        record.setScanTime(LocalDateTime.now());
        recordMapper.updateById(record);
        
        log.info("更新盘点记录状态: {} -> {}", recordId, status);
    }
}

package com.jujing.museum.modules.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jujing.museum.common.result.PageResult;
import com.jujing.museum.common.result.R;
import com.jujing.museum.modules.auth.entity.SysOperLog;
import com.jujing.museum.modules.auth.mapper.SysOperLogMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 操作日志
 */
@Tag(name = "操作日志")
@RestController
@RequestMapping("/api/v1/system/operlog")
@RequiredArgsConstructor
public class SysOperLogController {

    private final SysOperLogMapper operLogMapper;

    @Operation(summary = "分页查询操作日志")
    @SaCheckPermission("system:operlog:list")
    @GetMapping("/page")
    public R<PageResult<SysOperLog>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String operUser,
            @RequestParam(required = false) Integer businessType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime beginTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        Page<SysOperLog> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<SysOperLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(title), SysOperLog::getTitle, title);
        wrapper.like(StringUtils.hasText(operUser), SysOperLog::getOperUser, operUser);
        wrapper.eq(businessType != null, SysOperLog::getBusinessType, businessType);
        wrapper.eq(status != null, SysOperLog::getStatus, status);
        wrapper.ge(beginTime != null, SysOperLog::getOperTime, beginTime);
        wrapper.le(endTime != null, SysOperLog::getOperTime, endTime);
        wrapper.orderByDesc(SysOperLog::getOperTime);

        Page<SysOperLog> result = operLogMapper.selectPage(pageParam, wrapper);
        return R.ok(PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords()));
    }

    @Operation(summary = "批量删除操作日志")
    @SaCheckPermission("system:operlog:delete")
    @DeleteMapping("/{ids}")
    public R<Void> delete(@PathVariable String ids) {
        String[] idArr = ids.split(",");
        for (String id : idArr) {
            operLogMapper.deleteById(Long.parseLong(id.trim()));
        }
        return R.ok();
    }
}

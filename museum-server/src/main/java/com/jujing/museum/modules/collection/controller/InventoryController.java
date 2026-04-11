package com.jujing.museum.modules.collection.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jujing.museum.common.result.R;
import com.jujing.museum.modules.collection.dto.InventoryTaskDTO;
import com.jujing.museum.modules.collection.entity.InventoryRecord;
import com.jujing.museum.modules.collection.entity.InventoryTask;
import com.jujing.museum.modules.collection.service.InventoryTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 盘点管理
 */
@Tag(name = "盘点管理")
@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryTaskService taskService;

    // ==================== 盘点任务 ====================

    @Operation(summary = "任务列表")
    @GetMapping("/task/list")
    public R<IPage<InventoryTask>> taskList(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status
    ) {
        return R.ok(taskService.page(current, size, keyword, status));
    }

    @Operation(summary = "任务详情")
    @GetMapping("/task/{id}")
    public R<InventoryTask> getTaskById(@PathVariable Long id) {
        return R.ok(taskService.getById(id));
    }

    @Operation(summary = "创建任务")
    @PostMapping("/task")
    public R<Void> addTask(@RequestBody InventoryTaskDTO dto) {
        taskService.add(dto);
        return R.ok();
    }

    @Operation(summary = "开始盘点")
    @PutMapping("/task/start/{id}")
    public R<Void> startTask(@PathVariable Long id) {
        taskService.start(id);
        return R.ok();
    }

    @Operation(summary = "完成盘点")
    @PutMapping("/task/complete/{id}")
    public R<Void> completeTask(@PathVariable Long id) {
        taskService.complete(id);
        return R.ok();
    }

    @Operation(summary = "取消任务")
    @PutMapping("/task/cancel/{id}")
    public R<Void> cancelTask(@PathVariable Long id) {
        taskService.cancel(id);
        return R.ok();
    }

    @Operation(summary = "删除任务")
    @DeleteMapping("/task/{id}")
    public R<Void> deleteTask(@PathVariable Long id) {
        taskService.delete(id);
        return R.ok();
    }

    // ==================== 盘点记录 ====================

    @Operation(summary = "任务盘点记录")
    @GetMapping("/record/list")
    public R<IPage<InventoryRecord>> recordList(
            @Parameter(description = "任务ID") @RequestParam Long taskId,
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size
    ) {
        return R.ok(taskService.getRecords(taskId, current, size));
    }

    @Operation(summary = "记录详情")
    @GetMapping("/record/{id}")
    public R<InventoryRecord> getRecordById(@PathVariable Long id) {
        return R.ok(taskService.getRecords(id, 1, 1).getRecords().stream().findFirst().orElse(null));
    }

    @Operation(summary = "更新记录状态")
    @PutMapping("/record/status")
    public R<Void> updateRecordStatus(
            @RequestParam Long recordId,
            @RequestParam Integer status
    ) {
        taskService.updateRecordStatus(recordId, status);
        return R.ok();
    }
}

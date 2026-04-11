package com.jujing.museum.modules.collection.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jujing.museum.common.result.R;
import com.jujing.museum.modules.collection.dto.RepairOrderDTO;
import com.jujing.museum.modules.collection.entity.RepairOrder;
import com.jujing.museum.modules.collection.service.RepairOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 修复工单管理
 */
@Tag(name = "修复工单管理")
@RestController
@RequestMapping("/repair/order")
@RequiredArgsConstructor
public class RepairController {

    private final RepairOrderService orderService;

    @Operation(summary = "工单列表")
    @GetMapping("/list")
    public R<IPage<RepairOrder>> list(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status
    ) {
        return R.ok(orderService.page(current, size, keyword, status));
    }

    @Operation(summary = "工单详情")
    @GetMapping("/{id}")
    public R<RepairOrder> getById(@PathVariable Long id) {
        return R.ok(orderService.getById(id));
    }

    @Operation(summary = "创建工单")
    @PostMapping
    public R<Void> add(@RequestBody RepairOrderDTO dto) {
        orderService.add(dto);
        return R.ok();
    }

    @Operation(summary = "派单")
    @PutMapping("/dispatch/{id}")
    public R<Void> dispatch(
            @PathVariable Long id,
            @RequestParam Long handlerId
    ) {
        orderService.dispatch(id, handlerId);
        return R.ok();
    }

    @Operation(summary = "完成修复")
    @PutMapping("/complete/{id}")
    public R<Void> complete(
            @PathVariable Long id,
            @RequestParam(required = false) String repairer,
            @RequestParam(required = false) String repairUnit,
            @RequestParam(required = false) String repairPlan,
            @RequestParam(required = false) BigDecimal cost
    ) {
        orderService.complete(id, repairer, repairUnit, repairPlan, cost);
        return R.ok();
    }

    @Operation(summary = "取消工单")
    @PutMapping("/cancel/{id}")
    public R<Void> cancel(@PathVariable Long id) {
        orderService.cancel(id);
        return R.ok();
    }

    @Operation(summary = "删除工单")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        orderService.delete(id);
        return R.ok();
    }
}

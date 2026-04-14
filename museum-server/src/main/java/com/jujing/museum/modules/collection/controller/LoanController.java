package com.jujing.museum.modules.collection.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jujing.museum.common.result.R;
import com.jujing.museum.modules.collection.dto.LoanApplyDTO;
import com.jujing.museum.modules.collection.entity.LoanApply;
import com.jujing.museum.modules.collection.service.LoanApplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 外借申请管理
 */
@Tag(name = "外借申请管理")
@RestController
@RequestMapping("/api/v1/loan/apply")
@RequiredArgsConstructor
public class LoanController {

    private final LoanApplyService applyService;

    @Operation(summary = "申请列表")
    @GetMapping("/list")
    public R<IPage<LoanApply>> list(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status
    ) {
        return R.ok(applyService.page(current, size, keyword, status));
    }

    @Operation(summary = "申请详情")
    @GetMapping("/{id}")
    public R<LoanApply> getById(@PathVariable Long id) {
        return R.ok(applyService.getById(id));
    }

    @Operation(summary = "创建申请")
    @PostMapping
    public R<Void> add(@RequestBody LoanApplyDTO dto) {
        applyService.add(dto);
        return R.ok();
    }

    @Operation(summary = "审批通过")
    @PutMapping("/approve/{id}")
    public R<Void> approve(
            @PathVariable Long id,
            @RequestParam(required = false) String opinion
    ) {
        applyService.approve(id, opinion);
        return R.ok();
    }

    @Operation(summary = "审批拒绝")
    @PutMapping("/reject/{id}")
    public R<Void> reject(
            @PathVariable Long id,
            @RequestParam(required = false) String opinion
    ) {
        applyService.reject(id, opinion);
        return R.ok();
    }

    @Operation(summary = "撤回申请")
    @PutMapping("/cancel/{id}")
    public R<Void> cancel(@PathVariable Long id) {
        applyService.cancel(id);
        return R.ok();
    }

    @Operation(summary = "归还藏品")
    @PutMapping("/return/{id}")
    public R<Void> returnApply(
            @PathVariable Long id,
            @RequestParam(required = false) String returner,
            @RequestParam(required = false) String remark
    ) {
        applyService.returnApply(id, returner, remark);
        return R.ok();
    }

    @Operation(summary = "删除申请")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        applyService.delete(id);
        return R.ok();
    }
}

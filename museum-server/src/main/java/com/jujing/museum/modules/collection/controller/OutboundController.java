package com.jujing.museum.modules.collection.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jujing.museum.common.result.R;
import com.jujing.museum.modules.collection.dto.OutboundDTO;
import com.jujing.museum.modules.collection.service.OutboundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 出库控制器
 */
@Tag(name = "出库管理")
@RestController
@RequestMapping("/api/v1/outbound")
@RequiredArgsConstructor
public class OutboundController {

    private final OutboundService outboundService;

    @Operation(summary = "出库列表")
    @GetMapping("/list")
    public R<IPage<OutboundDTO>> list(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return R.ok(outboundService.pageList(current, size, keyword, status));
    }

    @Operation(summary = "出库详情")
    @GetMapping("/{id}")
    public R<OutboundDTO> getById(@PathVariable Long id) {
        return R.ok(outboundService.getById(id));
    }

    @Operation(summary = "新增出库")
    @PostMapping
    public R<Void> add(@Valid @RequestBody OutboundDTO dto) {
        outboundService.add(dto);
        return R.ok();
    }

    @Operation(summary = "审批出库")
    @PutMapping("/approve/{id}")
    public R<Void> approve(
            @PathVariable Long id,
            @RequestParam boolean approved,
            @RequestParam(required = false) String remark) {
        outboundService.approve(id, approved, remark);
        return R.ok();
    }

    @Operation(summary = "归还藏品")
    @PutMapping("/return/{id}")
    public R<Void> return_(@PathVariable Long id) {
        outboundService.return_(id);
        return R.ok();
    }

    @Operation(summary = "删除出库")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        outboundService.delete(id);
        return R.ok();
    }
}

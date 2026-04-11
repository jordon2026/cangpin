package com.jujing.museum.modules.collection.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jujing.museum.common.result.R;
import com.jujing.museum.modules.collection.dto.InboundDTO;
import com.jujing.museum.modules.collection.service.InboundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 入库控制器
 */
@Tag(name = "入库管理")
@RestController
@RequestMapping("/api/v1/inbound")
@RequiredArgsConstructor
public class InboundController {

    private final InboundService inboundService;

    @Operation(summary = "入库列表")
    @GetMapping("/list")
    public R<IPage<InboundDTO>> list(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return R.ok(inboundService.pageList(current, size, keyword, status));
    }

    @Operation(summary = "入库详情")
    @GetMapping("/{id}")
    public R<InboundDTO> getById(@PathVariable Long id) {
        return R.ok(inboundService.getById(id));
    }

    @Operation(summary = "新增入库")
    @PostMapping
    public R<Void> add(@Valid @RequestBody InboundDTO dto) {
        inboundService.add(dto);
        return R.ok();
    }

    @Operation(summary = "审批入库")
    @PutMapping("/approve/{id}")
    public R<Void> approve(
            @PathVariable Long id,
            @RequestParam boolean approved,
            @RequestParam(required = false) String remark) {
        inboundService.approve(id, approved, remark);
        return R.ok();
    }

    @Operation(summary = "删除入库")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        inboundService.delete(id);
        return R.ok();
    }
}

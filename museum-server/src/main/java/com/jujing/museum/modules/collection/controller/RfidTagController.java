package com.jujing.museum.modules.collection.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jujing.museum.common.result.R;
import com.jujing.museum.modules.collection.dto.RfidTagDTO;
import com.jujing.museum.modules.collection.entity.RfidTag;
import com.jujing.museum.modules.collection.service.RfidTagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * RFID标签管理
 */
@Tag(name = "RFID标签管理")
@RestController
@RequestMapping("/api/v1/rfid/tag")
@RequiredArgsConstructor
public class RfidTagController {

    private final RfidTagService tagService;

    @Operation(summary = "标签列表")
    @GetMapping("/list")
    public R<IPage<RfidTag>> list(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status
    ) {
        return R.ok(tagService.page(current, size, keyword, status));
    }

    @Operation(summary = "获取未绑定标签")
    @GetMapping("/unbound")
    public R<List<RfidTag>> getUnboundTags() {
        return R.ok(tagService.getUnboundTags());
    }

    @Operation(summary = "标签详情")
    @GetMapping("/{id}")
    public R<RfidTag> getById(@PathVariable Long id) {
        return R.ok(tagService.getById(id));
    }

    @Operation(summary = "新增标签")
    @PostMapping
    public R<Void> add(@RequestBody RfidTagDTO dto) {
        tagService.add(dto);
        return R.ok();
    }

    @Operation(summary = "更新标签")
    @PutMapping
    public R<Void> update(@RequestBody RfidTagDTO dto) {
        tagService.update(dto);
        return R.ok();
    }

    @Operation(summary = "解绑标签")
    @PutMapping("/unbind/{id}")
    public R<Void> unbind(@PathVariable Long id) {
        tagService.unbind(id);
        return R.ok();
    }

    @Operation(summary = "删除标签")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        tagService.delete(id);
        return R.ok();
    }
}

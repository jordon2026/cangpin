package com.jujing.museum.modules.collection.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jujing.museum.common.result.R;
import com.jujing.museum.modules.collection.dto.CollectionDTO;
import com.jujing.museum.modules.collection.service.CollectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 藏品控制器
 */
@Tag(name = "藏品管理")
@RestController
@RequestMapping("/api/v1/collection")
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionService collectionService;

    /**
     * 分页查询藏品列表
     */
    @Operation(summary = "藏品列表")
    @GetMapping("/list")
    public R<IPage<CollectionDTO>> list(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status) {
        return R.ok(collectionService.pageList(current, size, keyword, category, status));
    }

    /**
     * 获取藏品详情
     */
    @Operation(summary = "藏品详情")
    @GetMapping("/{id}")
    public R<CollectionDTO> getById(@PathVariable Long id) {
        return R.ok(collectionService.getById(id));
    }

    /**
     * 新增藏品
     */
    @Operation(summary = "新增藏品")
    @PostMapping
    public R<Void> add(@Valid @RequestBody CollectionDTO dto) {
        collectionService.add(dto);
        return R.ok();
    }

    /**
     * 更新藏品
     */
    @Operation(summary = "更新藏品")
    @PutMapping
    public R<Void> update(@Valid @RequestBody CollectionDTO dto) {
        collectionService.update(dto);
        return R.ok();
    }

    /**
     * 删除藏品
     */
    @Operation(summary = "删除藏品")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        collectionService.delete(id);
        return R.ok();
    }

    /**
     * 批量删除
     */
    @Operation(summary = "批量删除")
    @DeleteMapping("/batch")
    public R<Void> batchDelete(@RequestBody List<Long> ids) {
        collectionService.batchDelete(ids);
        return R.ok();
    }

    /**
     * 获取所有分类
     */
    @Operation(summary = "获取所有分类")
    @GetMapping("/categories")
    public R<List<String>> getCategories() {
        return R.ok(collectionService.getAllCategories());
    }
}

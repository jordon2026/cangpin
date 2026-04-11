package com.jujing.museum.modules.collection.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jujing.museum.common.result.R;
import com.jujing.museum.modules.collection.dto.StorageDTO;
import com.jujing.museum.modules.collection.service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 库房控制器
 */
@Tag(name = "库房管理")
@RestController
@RequestMapping("/api/v1/storage")
@RequiredArgsConstructor
public class StorageController {

    private final StorageService storageService;

    /**
     * 分页查询库房列表
     */
    @Operation(summary = "库房列表")
    @GetMapping("/list")
    public R<IPage<StorageDTO>> list(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return R.ok(storageService.pageList(current, size, keyword));
    }

    /**
     * 获取所有库房（下拉框用）
     */
    @Operation(summary = "所有库房")
    @GetMapping("/all")
    public R<List<StorageDTO>> getAll() {
        return R.ok(storageService.getAll());
    }

    /**
     * 获取库房详情
     */
    @Operation(summary = "库房详情")
    @GetMapping("/{id}")
    public R<StorageDTO> getById(@PathVariable Long id) {
        return R.ok(storageService.getById(id));
    }

    /**
     * 新增库房
     */
    @Operation(summary = "新增库房")
    @PostMapping
    public R<Void> add(@Valid @RequestBody StorageDTO dto) {
        storageService.add(dto);
        return R.ok();
    }

    /**
     * 更新库房
     */
    @Operation(summary = "更新库房")
    @PutMapping
    public R<Void> update(@Valid @RequestBody StorageDTO dto) {
        storageService.update(dto);
        return R.ok();
    }

    /**
     * 删除库房
     */
    @Operation(summary = "删除库房")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        storageService.delete(id);
        return R.ok();
    }
}

package com.jujing.museum.modules.collection.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.jujing.museum.common.result.R;
import com.jujing.museum.modules.collection.dto.RfidDeviceDTO;
import com.jujing.museum.modules.collection.entity.RfidDevice;
import com.jujing.museum.modules.collection.service.RfidDeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * RFID设备管理
 */
@Tag(name = "RFID设备管理")
@RestController
@RequestMapping("/api/v1/rfid/device")
@RequiredArgsConstructor
public class RfidDeviceController {

    private final RfidDeviceService deviceService;

    @Operation(summary = "设备列表")
    @GetMapping("/list")
    public R<IPage<RfidDevice>> list(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") int current,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status
    ) {
        return R.ok(deviceService.page(current, size, keyword, status));
    }

    @Operation(summary = "设备详情")
    @GetMapping("/{id}")
    public R<RfidDevice> getById(@PathVariable Long id) {
        return R.ok(deviceService.getById(id));
    }

    @Operation(summary = "新增设备")
    @PostMapping
    public R<Void> add(@RequestBody RfidDeviceDTO dto) {
        deviceService.add(dto);
        return R.ok();
    }

    @Operation(summary = "更新设备")
    @PutMapping
    public R<Void> update(@RequestBody RfidDeviceDTO dto) {
        deviceService.update(dto);
        return R.ok();
    }

    @Operation(summary = "删除设备")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        deviceService.delete(id);
        return R.ok();
    }

    @Operation(summary = "切换设备状态")
    @PutMapping("/status/{id}")
    public R<Void> toggleStatus(
            @PathVariable Long id,
            @RequestParam Integer status
    ) {
        deviceService.toggleStatus(id, status);
        return R.ok();
    }
}

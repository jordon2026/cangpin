package com.jujing.museum.modules.auth.controller;

import com.jujing.museum.common.result.R;
import com.jujing.museum.modules.auth.dto.CaptchaVO;
import com.jujing.museum.modules.auth.dto.LoginDTO;
import com.jujing.museum.modules.auth.dto.UserVO;
import com.jujing.museum.modules.auth.entity.SysMenu;
import com.jujing.museum.modules.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 认证控制器
 */
@Tag(name = "认证管理")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 获取验证码
     */
    @Operation(summary = "获取验证码")
    @GetMapping("/captcha")
    public R<CaptchaVO> getCaptcha() {
        return R.ok(authService.generateCaptcha());
    }

    /**
     * 用户登录
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public R<UserVO> login(@Valid @RequestBody LoginDTO dto) {
        return R.ok(authService.login(dto));
    }

    /**
     * 退出登录
     */
    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public R<Void> logout() {
        authService.logout();
        return R.ok();
    }

    /**
     * 获取当前用户信息
     */
    @Operation(summary = "获取当前用户信息")
    @GetMapping("/userinfo")
    public R<UserVO> getUserInfo() {
        return R.ok(authService.getCurrentUser());
    }

    /**
     * 获取当前用户菜单
     */
    @Operation(summary = "获取用户菜单")
    @GetMapping("/menus")
    public R<List<SysMenu>> getMenus() {
        return R.ok(authService.getMenuTree());
    }
}

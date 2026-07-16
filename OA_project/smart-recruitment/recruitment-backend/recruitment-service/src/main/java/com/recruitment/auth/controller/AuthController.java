package com.recruitment.auth.controller;

import com.recruitment.common.core.model.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    @PostMapping("/register")
    public Result<?> register() {
        // TODO: 注册
        return Result.success();
    }

    @PostMapping("/login")
    public Result<?> login() {
        // TODO: 登录
        return Result.success();
    }

    @PostMapping("/logout")
    public Result<?> logout() {
        // TODO: 退出
        return Result.success();
    }

    @GetMapping("/current-user")
    public Result<?> getCurrentUser() {
        // TODO: 获取当前用户
        return Result.success();
    }

    @PostMapping("/refresh-token")
    public Result<?> refreshToken() {
        // TODO: 刷新Token
        return Result.success();
    }

    @PostMapping("/change-password")
    public Result<?> changePassword() {
        // TODO: 修改密码
        return Result.success();
    }
}

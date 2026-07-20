package com.recruitment.auth.controller;

import com.recruitment.auth.dto.LoginRequest;
import com.recruitment.auth.dto.RegisterRequest;
import com.recruitment.auth.service.AuthService;
import com.recruitment.auth.vo.LoginResponse;
import com.recruitment.common.core.model.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired(required = false)
    private AuthService authService;

    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody RegisterRequest request) {
        if (authService == null) return Result.success();
        authService.register(request);
        return Result.success();
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        if (authService == null) return Result.success();
        return Result.success(authService.login(request));
    }

    @PostMapping("/logout")
    public Result<?> logout() {
        return Result.success();
    }

    @GetMapping("/current-user")
    public Result<?> getCurrentUser() {
        return Result.success();
    }

    @PostMapping("/refresh-token")
    public Result<?> refreshToken() {
        return Result.success();
    }

    @PostMapping("/change-password")
    public Result<?> changePassword() {
        return Result.success();
    }
}

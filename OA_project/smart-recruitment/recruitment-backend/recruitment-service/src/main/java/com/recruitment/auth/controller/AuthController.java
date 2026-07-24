package com.recruitment.auth.controller;

import com.recruitment.auth.dto.ChangePasswordRequest;
import com.recruitment.auth.dto.ForgotPasswordResetRequest;
import com.recruitment.auth.dto.ForgotPasswordSendCodeRequest;
import com.recruitment.auth.dto.LoginRequest;
import com.recruitment.auth.dto.RefreshTokenRequest;
import com.recruitment.auth.dto.RegisterRequest;
import com.recruitment.auth.dto.UpdateProfileRequest;
import com.recruitment.auth.service.AuthService;
import com.recruitment.auth.vo.LoginResponse;
import com.recruitment.common.core.model.Result;
import jakarta.servlet.http.HttpServletRequest;
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
    public Result<?> logout(HttpServletRequest httpRequest) {
        if (authService == null) return Result.success();
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId != null) {
            authService.logout(userId);
        }
        return Result.success();
    }

    @GetMapping("/current-user")
    public Result<?> getCurrentUser(HttpServletRequest httpRequest) {
        if (authService == null) return Result.success();
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录或登录已失效");
        }
        return Result.success(authService.getCurrentUser(userId));
    }

    @PutMapping("/profile")
    public Result<?> updateProfile(@Valid @RequestBody UpdateProfileRequest request,
                                    HttpServletRequest httpRequest) {
        if (authService == null) return Result.success();
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录或登录已失效");
        }
        return Result.success(authService.updateProfile(userId, request));
    }

    @PostMapping("/refresh-token")
    public Result<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        if (authService == null) return Result.success();
        return Result.success(authService.refreshToken(request.getRefreshToken()));
    }

    @PostMapping("/change-password")
    public Result<?> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                     HttpServletRequest httpRequest) {
        if (authService == null) return Result.success();
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录或登录已失效");
        }
        authService.changePassword(userId, request);
        return Result.success();
    }

    @PostMapping("/forgot-password/send-code")
    public Result<?> sendForgotPasswordCode(@Valid @RequestBody ForgotPasswordSendCodeRequest request) {
        if (authService == null) return Result.success();
        authService.sendForgotPasswordCode(request);
        return Result.success();
    }

    @PostMapping("/forgot-password/reset")
    public Result<?> resetForgotPassword(@Valid @RequestBody ForgotPasswordResetRequest request) {
        if (authService == null) return Result.success();
        authService.resetForgotPassword(request);
        return Result.success();
    }
}

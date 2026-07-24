package com.recruitment.auth.service;

import com.recruitment.auth.dto.ChangePasswordRequest;
import com.recruitment.auth.dto.ForgotPasswordResetRequest;
import com.recruitment.auth.dto.ForgotPasswordSendCodeRequest;
import com.recruitment.auth.dto.LoginRequest;
import com.recruitment.auth.dto.RegisterRequest;
import com.recruitment.auth.dto.UpdateProfileRequest;
import com.recruitment.auth.vo.LoginResponse;
import com.recruitment.system.vo.SysUserVO;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request);

    /**
     * 使用刷新令牌轮换访问令牌和刷新令牌
     */
    LoginResponse refreshToken(String refreshToken);

    /**
     * 用户注册
     */
    void register(RegisterRequest request);

    /**
     * 用户退出登录
     */
    void logout(Long userId);

    /**
     * 当前登录用户修改自己的密码
     */
    void changePassword(Long userId, ChangePasswordRequest request);

    /**
     * 发送忘记密码邮箱验证码。
     */
    void sendForgotPasswordCode(ForgotPasswordSendCodeRequest request);

    /**
     * 使用邮箱验证码重置密码。
     */
    void resetForgotPassword(ForgotPasswordResetRequest request);

    /**
     * 获取当前登录用户信息
     */
    SysUserVO getCurrentUser(Long userId);

    /**
     * 当前登录用户修改自己的个人资料
     */
    SysUserVO updateProfile(Long userId, UpdateProfileRequest request);
}

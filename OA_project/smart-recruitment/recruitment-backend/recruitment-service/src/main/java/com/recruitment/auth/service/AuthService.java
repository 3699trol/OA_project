package com.recruitment.auth.service;

import com.recruitment.auth.dto.ChangePasswordRequest;
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
     * 用户注册
     */
    void register(RegisterRequest request);

    /**
     * 当前登录用户修改自己的密码
     */
    void changePassword(Long userId, ChangePasswordRequest request);

    /**
     * 获取当前登录用户信息
     */
    SysUserVO getCurrentUser(Long userId);

    /**
     * 当前登录用户修改自己的个人资料
     */
    SysUserVO updateProfile(Long userId, UpdateProfileRequest request);
}

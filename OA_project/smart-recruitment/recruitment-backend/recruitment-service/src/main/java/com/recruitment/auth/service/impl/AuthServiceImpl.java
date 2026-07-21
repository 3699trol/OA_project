package com.recruitment.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recruitment.auth.dto.ChangePasswordRequest;
import com.recruitment.auth.dto.LoginRequest;
import com.recruitment.auth.dto.RegisterRequest;
import com.recruitment.auth.dto.UpdateProfileRequest;
import com.recruitment.auth.service.AuthService;
import com.recruitment.auth.vo.LoginResponse;
import com.recruitment.common.core.exception.BusinessException;
import com.recruitment.common.security.jwt.JwtUtil;
import com.recruitment.system.entity.SysUser;
import com.recruitment.system.mapper.SysUserMapper;
import com.recruitment.system.vo.SysUserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 认证服务实现
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public LoginResponse login(LoginRequest request) {
        String loginAccount = request.getUsername().trim();
        List<SysUser> matchedUsers = sysUserMapper.selectList(
                new LambdaQueryWrapper<SysUser>()
                        .and(wrapper -> wrapper
                                .eq(SysUser::getUsername, loginAccount)
                                .or()
                                .eq(SysUser::getEmail, loginAccount))
                        .eq(SysUser::getDeleted, 0)
                        .last("LIMIT 2"));
        if (matchedUsers.size() != 1) {
            throw new BusinessException("用户名、邮箱或密码错误");
        }
        SysUser user = matchedUsers.get(0);
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名、邮箱或密码错误");
        }

        // 生成 JWT
        String role = resolveRole(user.getUserType());
        
        // 验证角色是否匹配
        if (request.getExpectedRole() != null && !request.getExpectedRole().isEmpty()) {
            if (!role.equals(request.getExpectedRole())) {
                throw new BusinessException("角色类型不匹配，请选择正确的角色登录");
            }
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), role);

        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        sysUserMapper.updateById(user);

        // 构造响应
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserInfo(new LoginResponse.UserInfo(
                user.getId(),
                user.getUsername(),
                role,
                user.getRealName() != null ? user.getRealName() : user.getUsername(),
                user.getAvatarUrl() != null ? user.getAvatarUrl() : ""
        ));
        return response;
    }

    @Override
    public void register(RegisterRequest request) {
        // 检查用户名是否已存在
        Long count = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, request.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 创建用户，默认为求职者
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setUserType(request.getUserType() != null ? request.getUserType() : 4); // 默认求职者
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        sysUserMapper.insert(user);
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码不正确");
        }
        if (request.getNewPassword().length() < 6) {
            throw new BusinessException("新密码长度不能少于6位");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        sysUserMapper.updateById(user);
    }

    @Override
    public SysUserVO getCurrentUser(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        SysUserVO vo = new SysUserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    @Override
    public SysUserVO updateProfile(Long userId, UpdateProfileRequest request) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (StringUtils.hasText(request.getRealName())) {
            user.setRealName(request.getRealName());
        }
        if (StringUtils.hasText(request.getEmail())) {
            user.setEmail(request.getEmail());
        }
        if (StringUtils.hasText(request.getPhone())) {
            user.setPhone(request.getPhone());
        }
        if (StringUtils.hasText(request.getAvatarUrl())) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        sysUserMapper.updateById(user);

        SysUserVO vo = new SysUserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    /**
     * 将 userType 数字转为角色编码
     */
    private String resolveRole(Integer userType) {
        if (userType == null) return "CANDIDATE";
        return switch (userType) {
            case 1 -> "ADMIN";
            case 2 -> "HR";
            case 3 -> "INTERVIEWER";
            default -> "CANDIDATE";
        };
    }
}

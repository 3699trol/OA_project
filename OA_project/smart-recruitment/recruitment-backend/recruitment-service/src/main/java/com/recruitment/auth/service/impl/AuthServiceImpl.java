package com.recruitment.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recruitment.auth.dto.ChangePasswordRequest;
import com.recruitment.auth.dto.LoginRequest;
import com.recruitment.auth.dto.RegisterRequest;
import com.recruitment.auth.dto.UpdateProfileRequest;
import com.recruitment.auth.service.AuthService;
import com.recruitment.auth.vo.LoginResponse;
import com.recruitment.common.core.exception.BusinessException;
import com.recruitment.common.redis.util.RedisUtil;
import com.recruitment.common.security.jwt.JwtUtil;
import com.recruitment.system.entity.SysRole;
import com.recruitment.system.entity.SysUser;
import com.recruitment.system.mapper.SysRoleMapper;
import com.recruitment.system.mapper.SysUserMapper;
import com.recruitment.system.vo.SysUserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private static final String USER_CACHE_PREFIX = "auth:user:";
    private static final String LOGIN_FAIL_PREFIX = "auth:login:fail:";
    private static final long USER_CACHE_TTL_MINUTES = 30;
    private static final long LOGIN_FAIL_TTL_MINUTES = 10;
    private static final long MAX_LOGIN_FAIL_COUNT = 5;

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final ObjectProvider<RedisUtil> redisUtilProvider;

    @Override
    public LoginResponse login(LoginRequest request) {
        String loginAccount = request.getUsername().trim();
        checkLoginRateLimit(loginAccount);
        List<SysUser> matchedUsers = sysUserMapper.selectList(
                new LambdaQueryWrapper<SysUser>()
                        .and(wrapper -> wrapper
                                .eq(SysUser::getUsername, loginAccount)
                                .or()
                                .eq(SysUser::getEmail, loginAccount))
                        .eq(SysUser::getDeleted, 0)
                        .last("LIMIT 2"));
        if (matchedUsers.size() != 1) {
            recordLoginFailure(loginAccount);
            throw new BusinessException("用户名、邮箱或密码错误");
        }
        SysUser user = matchedUsers.get(0);
        if (user.getStatus() != null && user.getStatus() == 0) {
            recordLoginFailure(loginAccount);
            throw new BusinessException("账号已被禁用");
        }

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            recordLoginFailure(loginAccount);
            throw new BusinessException("用户名、邮箱或密码错误");
        }

        String role = resolveRole(user.getUserType());

        // 校验该用户所属角色是否已被禁用
        checkRoleEnabled(role);

        // 验证角色是否匹配
        if (request.getExpectedRole() != null && !request.getExpectedRole().isEmpty()) {
            if (!role.equals(request.getExpectedRole())) {
                recordLoginFailure(loginAccount);
                throw new BusinessException("角色类型不匹配，请选择正确的角色登录");
            }
        }
        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        sysUserMapper.updateById(user);
        clearLoginFailure(loginAccount);

        return issueTokens(user, role);
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        if (!jwtUtil.validateRefreshToken(refreshToken)) {
            throw new BusinessException(401, "刷新令牌无效或已过期，请重新登录");
        }

        Long userId = jwtUtil.getUserId(refreshToken);
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || Integer.valueOf(1).equals(user.getDeleted())) {
            throw new BusinessException(401, "用户不存在或已被删除，请重新登录");
        }
        if (Integer.valueOf(0).equals(user.getStatus())) {
            throw new BusinessException(401, "账号已被禁用");
        }

        String role = resolveRole(user.getUserType());
        checkRoleEnabled(role);

        return issueTokens(user, role);
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
    public void logout(Long userId) {
        evictUserCache(userId);
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
        evictUserCache(userId);
    }

    @Override
    public SysUserVO getCurrentUser(Long userId) {
        SysUserVO cachedUser = getCachedUser(userId);
        if (cachedUser != null) {
            return cachedUser;
        }

        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        SysUserVO vo = toSysUserVO(user);
        cacheUser(vo);
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

        SysUserVO vo = toSysUserVO(user);
        cacheUser(vo);
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

    /**
     * 校验用户所属角色是否处于启用状态，被禁用的角色不允许登录
     */
    private void checkRoleEnabled(String roleCode) {
        if (roleCode == null) return;
        SysRole role = sysRoleMapper.selectOne(
                new LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getRoleCode, roleCode)
                        .last("LIMIT 1"));
        if (role != null && Integer.valueOf(0).equals(role.getStatus())) {
            throw new BusinessException("该角色已被禁用，无法登录，请联系管理员");
        }
    }

    private LoginResponse issueTokens(SysUser user, String role) {
        cacheUser(toSysUserVO(user));
        LoginResponse response = new LoginResponse();
        response.setToken(jwtUtil.generateToken(user.getId(), user.getUsername(), role));
        response.setRefreshToken(jwtUtil.generateRefreshToken(user.getId(), user.getUsername(), role));
        response.setUserInfo(new LoginResponse.UserInfo(
                user.getId(),
                user.getUsername(),
                role,
                user.getRealName() != null ? user.getRealName() : user.getUsername(),
                user.getAvatarUrl() != null ? user.getAvatarUrl() : ""
        ));
        return response;
    }

    private SysUserVO toSysUserVO(SysUser user) {
        SysUserVO vo = new SysUserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    private SysUserVO getCachedUser(Long userId) {
        RedisUtil redisUtil = redisUtilProvider.getIfAvailable();
        if (redisUtil == null || userId == null) {
            return null;
        }
        try {
            Object cached = redisUtil.get(userCacheKey(userId));
            if (cached instanceof SysUserVO user) {
                return user;
            }
        } catch (RuntimeException e) {
            log.warn("Failed to read user {} from Redis cache: {}", userId, e.getMessage());
        }
        return null;
    }

    private void cacheUser(SysUserVO user) {
        RedisUtil redisUtil = redisUtilProvider.getIfAvailable();
        if (redisUtil == null || user == null || user.getId() == null) {
            return;
        }
        try {
            redisUtil.set(userCacheKey(user.getId()), user, USER_CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (RuntimeException e) {
            log.warn("Failed to write user {} to Redis cache: {}", user.getId(), e.getMessage());
        }
    }

    private void evictUserCache(Long userId) {
        RedisUtil redisUtil = redisUtilProvider.getIfAvailable();
        if (redisUtil == null || userId == null) {
            return;
        }
        try {
            redisUtil.delete(userCacheKey(userId));
        } catch (RuntimeException e) {
            log.warn("Failed to evict user {} from Redis cache: {}", userId, e.getMessage());
        }
    }

    private void checkLoginRateLimit(String loginAccount) {
        RedisUtil redisUtil = redisUtilProvider.getIfAvailable();
        if (redisUtil == null) {
            return;
        }
        try {
            Object value = redisUtil.get(loginFailKey(loginAccount));
            if (value instanceof Number count && count.longValue() >= MAX_LOGIN_FAIL_COUNT) {
                throw new BusinessException(429, "登录失败次数过多，请稍后再试");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (RuntimeException e) {
            log.warn("Failed to check login rate limit for {}: {}", loginAccount, e.getMessage());
        }
    }

    private void recordLoginFailure(String loginAccount) {
        RedisUtil redisUtil = redisUtilProvider.getIfAvailable();
        if (redisUtil == null) {
            return;
        }
        try {
            Long count = redisUtil.increment(loginFailKey(loginAccount));
            if (count != null && count == 1L) {
                redisUtil.expire(loginFailKey(loginAccount), LOGIN_FAIL_TTL_MINUTES, TimeUnit.MINUTES);
            }
        } catch (RuntimeException e) {
            log.warn("Failed to record login failure for {}: {}", loginAccount, e.getMessage());
        }
    }

    private void clearLoginFailure(String loginAccount) {
        RedisUtil redisUtil = redisUtilProvider.getIfAvailable();
        if (redisUtil == null) {
            return;
        }
        try {
            redisUtil.delete(loginFailKey(loginAccount));
        } catch (RuntimeException e) {
            log.warn("Failed to clear login failure for {}: {}", loginAccount, e.getMessage());
        }
    }

    private String userCacheKey(Long userId) {
        return USER_CACHE_PREFIX + userId;
    }

    private String loginFailKey(String loginAccount) {
        return LOGIN_FAIL_PREFIX + loginAccount.trim().toLowerCase(Locale.ROOT);
    }
}

package com.recruitment.system.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruitment.system.entity.SysUser;
import com.recruitment.system.mapper.SysUserMapper;
import com.recruitment.common.core.exception.BusinessException;
import com.recruitment.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Set;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    private static final Set<String> SEARCH_FIELDS = Set.of(
            "all", "username", "realName", "phone", "email");

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Page<SysUser> listByPage(long pageNum, long pageSize, String keyword, String searchField,
                                    Integer userType, Integer status, Integer deleted) {
        validateListParameters(pageNum, pageSize, searchField, userType, status, deleted);

        String normalizedKeyword = StringUtils.hasText(keyword) ? keyword.trim() : null;
        String normalizedSearchField = StringUtils.hasText(searchField) ? searchField.trim() : "all";

        // 使用手写 SQL 查询，完全绕开 MyBatis-Plus 自动追加的逻辑删除过滤条件，
        // 确保已删除用户也能被查询出来（用于前端灰显 + 恢复）
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        return sysUserMapper.selectPageIncludeDeleted(
                page, normalizedKeyword, normalizedSearchField, userType, status, deleted);
    }

    private void validateListParameters(long pageNum, long pageSize, String searchField,
                                        Integer userType, Integer status, Integer deleted) {
        if (pageNum < 1) {
            throw new BusinessException(400, "页码必须大于等于 1");
        }
        if (pageSize < 1 || pageSize > 100) {
            throw new BusinessException(400, "每页数量必须在 1 到 100 之间");
        }

        String normalizedSearchField = StringUtils.hasText(searchField) ? searchField.trim() : "all";
        if (!SEARCH_FIELDS.contains(normalizedSearchField)) {
            throw new BusinessException(400, "不支持的用户搜索字段");
        }
        if (userType != null && (userType < 1 || userType > 4)) {
            throw new BusinessException(400, "用户类型必须为 1 到 4");
        }
        if (status != null && status != 0 && status != 1) {
            throw new BusinessException(400, "账号状态必须为 0 或 1");
        }
        if (deleted != null && deleted != 0 && deleted != 1) {
            throw new BusinessException(400, "删除状态必须为 0 或 1");
        }
    }

    @Override
    public SysUser getById(Long id) {
        return sysUserMapper.selectById(id);
    }

    @Override
    public void resetPassword(Long id) {
        log.info("开始重置用户密码，用户ID: {}", id);
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            log.warn("用户不存在，ID: {}", id);
            throw new BusinessException("用户不存在");
        }
        log.info("找到用户: {}, 开始加密新密码", user.getUsername());
        // 重置密码为 123456
        String encodedPassword = passwordEncoder.encode("123456");
        user.setPassword(encodedPassword);
        log.info("密码加密完成，开始更新数据库");
        int result = sysUserMapper.updateById(user);
        log.info("数据库更新结果: {}, 用户: {}", result, user.getUsername());
    }

    @Override
    public void deleteUser(Long id) {
        log.info("开始删除用户，用户ID: {}", id);
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            log.warn("用户不存在，ID: {}", id);
            throw new BusinessException("用户不存在");
        }
        log.info("找到用户: {}, 开始执行逻辑删除", user.getUsername());
        // MyBatis-Plus 逻辑删除
        int result = sysUserMapper.deleteById(id);
        log.info("逻辑删除结果: {}, 用户: {}", result, user.getUsername());
    }

    @Override
    public void restoreUser(Long id) {
        log.info("开始恢复用户，用户ID: {}", id);
        // 使用手写 SQL 完全绕开逻辑删除的自动过滤条件
        int result = sysUserMapper.restoreById(id);
        log.info("恢复用户结果: {}, 用户ID: {}", result, id);
        if (result == 0) {
            throw new BusinessException("用户不存在或已处于正常状态");
        }
    }

    @Override
    public void updateUser(SysUser user) {
        log.info("开始更新用户信息，用户ID: {}", user.getId());
        SysUser existing = sysUserMapper.selectById(user.getId());
        if (existing == null) {
            throw new BusinessException("用户不存在");
        }
        // 仅允许更新这些非敏感字段，防止覆盖密码等敏感数据
        if (org.springframework.util.StringUtils.hasText(user.getRealName())) {
            existing.setRealName(user.getRealName());
        }
        if (user.getUserType() != null) {
            existing.setUserType(user.getUserType());
        }
        if (user.getGender() != null) {
            existing.setGender(user.getGender());
        }
        if (user.getPhone() != null) {
            existing.setPhone(user.getPhone());
        }
        if (user.getEmail() != null) {
            existing.setEmail(user.getEmail());
        }
        if (user.getStatus() != null) {
            existing.setStatus(user.getStatus());
        }
        existing.setUpdateTime(java.time.LocalDateTime.now());
        sysUserMapper.updateById(existing);
        log.info("用户 {} 信息更新成功", user.getId());
    }
}

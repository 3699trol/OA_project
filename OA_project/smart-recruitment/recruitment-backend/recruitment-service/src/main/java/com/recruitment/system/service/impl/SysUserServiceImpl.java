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

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Page<SysUser> listByPage(long pageNum, long pageSize, String keyword) {
        // 使用手写 SQL 查询，完全绕开 MyBatis-Plus 自动追加的逻辑删除过滤条件，
        // 确保已删除用户也能被查询出来（用于前端灰显 + 恢复）
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        return sysUserMapper.selectPageIncludeDeleted(page, keyword);
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
}

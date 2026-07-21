package com.recruitment.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recruitment.system.entity.SysRole;
import com.recruitment.system.mapper.SysRoleMapper;
import com.recruitment.system.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 角色服务实现
 */
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl implements SysRoleService {

    private final SysRoleMapper sysRoleMapper;

    @Override
    public List<SysRole> listAll() {
        return sysRoleMapper.selectList(
                new LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getStatus, 1)
                        .orderByAsc(SysRole::getId));
    }

    @Override
    public SysRole getById(Long id) {
        return sysRoleMapper.selectById(id);
    }

    @Override
    public boolean updateRole(SysRole sysRole) {
        sysRole.setUpdateTime(java.time.LocalDateTime.now());
        return sysRoleMapper.updateById(sysRole) > 0;
    }
}

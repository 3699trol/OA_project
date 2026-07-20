package com.recruitment.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recruitment.system.entity.SysPermission;
import com.recruitment.system.mapper.SysPermissionMapper;
import com.recruitment.system.service.SysPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 权限服务实现
 */
@Service
@RequiredArgsConstructor
public class SysPermissionServiceImpl implements SysPermissionService {

    private final SysPermissionMapper sysPermissionMapper;

    @Override
    public List<SysPermission> listAll() {
        return sysPermissionMapper.selectList(
                new LambdaQueryWrapper<SysPermission>()
                        .eq(SysPermission::getStatus, 1)
                        .orderByAsc(SysPermission::getId));
    }
}

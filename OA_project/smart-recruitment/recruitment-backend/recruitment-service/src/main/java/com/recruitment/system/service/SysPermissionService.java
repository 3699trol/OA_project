package com.recruitment.system.service;

import com.recruitment.system.entity.SysPermission;

import java.util.List;

/**
 * 权限服务接口
 */
public interface SysPermissionService {

    /**
     * 查询所有权限
     */
    List<SysPermission> listAll();
}

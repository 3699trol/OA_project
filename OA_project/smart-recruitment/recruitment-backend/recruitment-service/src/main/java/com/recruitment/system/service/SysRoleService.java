package com.recruitment.system.service;

import com.recruitment.system.entity.SysRole;

import java.util.List;

/**
 * 角色服务接口
 */
public interface SysRoleService {

    /**
     * 查询所有角色
     */
    List<SysRole> listAll();

    /**
     * 根据ID查询角色
     */
    SysRole getById(Long id);

    /**
     * 更新角色
     */
    boolean updateRole(SysRole sysRole);
}

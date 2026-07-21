package com.recruitment.system.controller;

import com.recruitment.common.core.model.Result;
import com.recruitment.system.entity.SysRole;
import com.recruitment.system.service.SysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器
 */
@RestController
@RequestMapping("/api/system/role")
public class RoleController {

    @Autowired(required = false)
    private SysRoleService sysRoleService;

    @GetMapping("/list")
    public Result<List<SysRole>> list() {
        if (sysRoleService == null) return Result.success();
        return Result.success(sysRoleService.listAll());
    }

    @GetMapping("/{id}")
    public Result<SysRole> getById(@PathVariable Long id) {
        if (sysRoleService == null) return Result.success();
        SysRole role = sysRoleService.getById(id);
        if (role == null) {
            return Result.error("角色不存在");
        }
        return Result.success(role);
    }

    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody SysRole sysRole) {
        if (sysRoleService == null) return Result.success();
        sysRole.setId(id);
        boolean success = sysRoleService.updateRole(sysRole);
        return success ? Result.success(true) : Result.error("更新失败");
    }
}

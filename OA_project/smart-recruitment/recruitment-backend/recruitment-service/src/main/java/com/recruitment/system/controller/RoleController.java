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
}

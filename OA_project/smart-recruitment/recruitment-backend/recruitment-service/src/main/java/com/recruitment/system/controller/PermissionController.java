package com.recruitment.system.controller;

import com.recruitment.common.core.model.Result;
import com.recruitment.system.entity.SysPermission;
import com.recruitment.system.service.SysPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限管理控制器
 */
@RestController
@RequestMapping("/api/system/permission")
public class PermissionController {

    @Autowired(required = false)
    private SysPermissionService sysPermissionService;

    @GetMapping("/list")
    public Result<List<SysPermission>> list() {
        if (sysPermissionService == null) return Result.success();
        return Result.success(sysPermissionService.listAll());
    }
}

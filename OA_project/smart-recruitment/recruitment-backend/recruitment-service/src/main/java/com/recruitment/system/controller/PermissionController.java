package com.recruitment.system.controller;

import com.recruitment.common.core.model.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 权限管理控制器
 */
@RestController
@RequestMapping("/api/system/permission")
@RequiredArgsConstructor
public class PermissionController {

    @GetMapping("/list")
    public Result<?> list() {
        // TODO: 权限列表
        return Result.success();
    }
}

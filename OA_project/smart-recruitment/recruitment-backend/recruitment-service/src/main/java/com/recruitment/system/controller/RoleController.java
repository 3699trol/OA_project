package com.recruitment.system.controller;

import com.recruitment.common.core.model.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 角色管理控制器
 */
@RestController
@RequestMapping("/api/system/role")
@RequiredArgsConstructor
public class RoleController {

    @GetMapping("/list")
    public Result<?> list() {
        // TODO: 角色列表
        return Result.success();
    }
}

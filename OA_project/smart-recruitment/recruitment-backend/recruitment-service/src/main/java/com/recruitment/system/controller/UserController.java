package com.recruitment.system.controller;

import com.recruitment.common.core.model.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/api/system/user")
@RequiredArgsConstructor
public class UserController {

    // TODO: 注入UserService
    // private final UserService userService;

    @GetMapping("/list")
    public Result<?> list() {
        // TODO: 用户列表
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        // TODO: 根据ID查询用户
        return Result.success();
    }
}

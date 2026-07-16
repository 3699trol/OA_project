package com.recruitment.application.controller;

import com.recruitment.common.core.model.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 职位投递控制器
 */
@RestController
@RequestMapping("/api/application")
@RequiredArgsConstructor
public class JobApplicationController {

    @PostMapping
    public Result<?> create() {
        // TODO: 投递职位
        return Result.success();
    }

    @GetMapping("/list")
    public Result<?> list() {
        // TODO: 投递记录
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id) {
        // TODO: 修改投递状态
        return Result.success();
    }
}

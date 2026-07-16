package com.recruitment.interview.controller;

import com.recruitment.common.core.model.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 面试管理控制器
 */
@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
public class InterviewController {

    @PostMapping
    public Result<?> create() {
        // TODO: 创建面试任务
        return Result.success();
    }

    @GetMapping("/list")
    public Result<?> list() {
        // TODO: 面试列表
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        // TODO: 面试详情
        return Result.success();
    }
}

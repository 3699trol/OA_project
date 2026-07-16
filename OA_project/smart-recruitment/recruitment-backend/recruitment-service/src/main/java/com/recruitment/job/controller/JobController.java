package com.recruitment.job.controller;

import com.recruitment.common.core.model.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 职位管理控制器
 */
@RestController
@RequestMapping("/api/job")
@RequiredArgsConstructor
public class JobController {

    @GetMapping("/list")
    public Result<?> list() {
        // TODO: 职位列表
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        // TODO: 职位详情
        return Result.success();
    }

    @PostMapping
    public Result<?> create() {
        // TODO: 创建职位
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id) {
        // TODO: 更新职位
        return Result.success();
    }

    @PostMapping("/{id}/publish")
    public Result<?> publish(@PathVariable Long id) {
        // TODO: 发布职位
        return Result.success();
    }

    @PostMapping("/{id}/unpublish")
    public Result<?> unpublish(@PathVariable Long id) {
        // TODO: 下架职位
        return Result.success();
    }
}

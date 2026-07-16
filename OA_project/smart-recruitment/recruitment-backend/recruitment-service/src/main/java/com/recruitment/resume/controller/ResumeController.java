package com.recruitment.resume.controller;

import com.recruitment.common.core.model.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 简历管理控制器
 */
@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
public class ResumeController {

    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        // TODO: 获取简历详情
        return Result.success();
    }

    @PostMapping
    public Result<?> save() {
        // TODO: 保存简历
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id) {
        // TODO: 更新简历
        return Result.success();
    }

    @PostMapping("/{id}/parse")
    public Result<?> parse(@PathVariable Long id) {
        // TODO: AI解析简历
        return Result.success();
    }

    @PostMapping("/{id}/evaluate")
    public Result<?> evaluate(@PathVariable Long id) {
        // TODO: AI简历评估
        return Result.success();
    }
}

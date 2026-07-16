package com.recruitment.ai.mockinterview.controller;

import com.recruitment.common.core.model.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 模拟面试控制器（求职者权限）
 */
@RestController
@RequestMapping("/api/ai/mock-interview")
@RequiredArgsConstructor
public class MockInterviewController {

    @PostMapping("/generate")
    public Result<?> generate() {
        // TODO: 生成模拟面试题
        return Result.success();
    }

    @PostMapping("/record")
    public Result<?> recordAnswer() {
        // TODO: 记录用户回答
        return Result.success();
    }

    @PostMapping("/follow-up")
    public Result<?> followUp() {
        // TODO: 生成追问
        return Result.success();
    }

    @GetMapping("/report/{sessionId}")
    public Result<?> getReport(@PathVariable Long sessionId) {
        // TODO: 生成模拟面试报告
        return Result.success();
    }
}

package com.recruitment.interview.controller;

import com.recruitment.common.core.model.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 面试题管理控制器
 */
@RestController
@RequestMapping("/api/interview/question")
@RequiredArgsConstructor
public class InterviewQuestionController {

    @PostMapping("/generate")
    public Result<?> generate() {
        // TODO: AI生成面试题
        return Result.success();
    }

    @PostMapping
    public Result<?> save() {
        // TODO: 保存正式面试题
        return Result.success();
    }

    @GetMapping("/{interviewId}")
    public Result<?> listByInterview(@PathVariable Long interviewId) {
        // TODO: 根据面试ID查询面试题
        return Result.success();
    }
}

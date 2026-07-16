package com.recruitment.interview.controller;

import com.recruitment.common.core.model.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 面试评价控制器
 */
@RestController
@RequestMapping("/api/interview/evaluation")
@RequiredArgsConstructor
public class InterviewEvaluationController {

    @PostMapping
    public Result<?> save() {
        // TODO: 填写面试评价
        return Result.success();
    }

    @GetMapping("/{interviewId}")
    public Result<?> getByInterview(@PathVariable Long interviewId) {
        // TODO: 根据面试ID查询评价
        return Result.success();
    }
}

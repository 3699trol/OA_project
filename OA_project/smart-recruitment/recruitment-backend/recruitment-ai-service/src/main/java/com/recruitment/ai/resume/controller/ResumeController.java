package com.recruitment.ai.resume.controller;

import com.recruitment.api.dto.AiResumeParseRequest;
import com.recruitment.api.dto.AiResumeParseResponse;
import com.recruitment.common.core.model.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * AI简历解析控制器
 */
@RestController
@RequestMapping("/api/ai/resume")
@RequiredArgsConstructor
public class ResumeController {

    @PostMapping("/parse")
    public Result<AiResumeParseResponse> parse(@RequestBody AiResumeParseRequest request) {
        // TODO: AI简历结构化解析
        return Result.success();
    }

    @PostMapping("/evaluate")
    public Result<?> evaluate(@RequestBody String resumeContent) {
        // TODO: AI简历质量评估
        return Result.success();
    }

    @PostMapping("/optimize")
    public Result<?> optimize(@RequestBody String resumeContent) {
        // TODO: AI简历优化建议
        return Result.success();
    }
}

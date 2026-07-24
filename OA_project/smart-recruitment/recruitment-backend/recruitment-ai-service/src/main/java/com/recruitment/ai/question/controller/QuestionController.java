package com.recruitment.ai.question.controller;

import com.recruitment.api.dto.AiQuestionRequest;
import com.recruitment.api.dto.AiQuestionResponse;
import com.recruitment.ai.question.service.QuestionService;
import com.recruitment.common.core.model.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 正式面试题生成控制器（面试官权限）
 */
@RestController
@RequestMapping("/api/ai/question")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping("/generate")
    public Result<AiQuestionResponse> generate(@Valid @RequestBody AiQuestionRequest request) {
        return Result.success(questionService.generate(request));
    }
}

package com.recruitment.ai.question.controller;

import com.recruitment.api.dto.AiQuestionRequest;
import com.recruitment.api.dto.AiQuestionResponse;
import com.recruitment.common.core.model.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 正式面试题生成控制器（面试官权限）
 */
@RestController
@RequestMapping("/api/ai/question")
@RequiredArgsConstructor
public class QuestionController {

    @PostMapping("/generate")
    public Result<AiQuestionResponse> generate(@RequestBody AiQuestionRequest request) {
        // TODO: AI生成面试题
        return Result.success();
    }
}

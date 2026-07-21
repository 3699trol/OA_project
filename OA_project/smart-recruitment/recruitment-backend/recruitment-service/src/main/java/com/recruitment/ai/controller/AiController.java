package com.recruitment.ai.controller;

import com.recruitment.api.client.AiServiceClient;
import com.recruitment.api.dto.AiMatchRequest;
import com.recruitment.api.dto.AiMatchResponse;
import com.recruitment.api.dto.AiQuestionRequest;
import com.recruitment.api.dto.AiQuestionResponse;
import com.recruitment.api.dto.AiResumeParseRequest;
import com.recruitment.api.dto.AiResumeParseResponse;
import com.recruitment.common.core.model.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 核心服务的 AI 接口入口，统一鉴权后转发给独立 AI 服务。
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiServiceClient aiServiceClient;

    @PostMapping("/resume/parse")
    public Result<AiResumeParseResponse> parseResume(@RequestBody AiResumeParseRequest request) {
        return aiServiceClient.parseResume(request);
    }

    @PostMapping("/match")
    public Result<AiMatchResponse> match(@RequestBody AiMatchRequest request) {
        return aiServiceClient.matchResumeAndJob(request);
    }

    @PostMapping("/question/generate")
    public Result<AiQuestionResponse> generateQuestions(@RequestBody AiQuestionRequest request) {
        return aiServiceClient.generateQuestions(request);
    }
}

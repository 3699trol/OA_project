package com.recruitment.api.client;

import com.recruitment.api.dto.*;
import com.recruitment.common.core.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * AI服务Feign客户端
 */
@FeignClient(name = "recruitment-ai-service")
public interface AiServiceClient {

    @PostMapping("/api/ai/resume/parse")
    Result<AiResumeParseResponse> parseResume(@RequestBody AiResumeParseRequest request);

    @PostMapping("/api/ai/match")
    Result<AiMatchResponse> matchResumeAndJob(@RequestBody AiMatchRequest request);

    @PostMapping("/api/ai/question/generate")
    Result<AiQuestionResponse> generateQuestions(@RequestBody AiQuestionRequest request);
}

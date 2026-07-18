package com.recruitment.api.client;

import com.recruitment.api.dto.*;
import com.recruitment.common.core.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * AI服务Feign客户端
 * 本地模式通过 url 直连，不依赖 Nacos 服务发现
 * dev模式将 url 留空，通过服务名从 Nacos 选择实例
 */
@FeignClient(name = "recruitment-ai-service", url = "${ai.service.url:}")
public interface AiServiceClient {

    @PostMapping("/api/ai/resume/parse")
    Result<AiResumeParseResponse> parseResume(@RequestBody AiResumeParseRequest request);

    @PostMapping("/api/ai/match")
    Result<AiMatchResponse> matchResumeAndJob(@RequestBody AiMatchRequest request);

    @PostMapping("/api/ai/question/generate")
    Result<AiQuestionResponse> generateQuestions(@RequestBody AiQuestionRequest request);
}

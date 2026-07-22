package com.recruitment.ai.mockinterview.controller;

import com.recruitment.ai.mockinterview.service.MockInterviewService;
import com.recruitment.api.dto.AiMockInterviewChatRequest;
import com.recruitment.api.dto.AiMockInterviewChatResponse;
import com.recruitment.api.dto.AiMockInterviewStartRequest;
import com.recruitment.api.dto.AiMockInterviewStartResponse;
import com.recruitment.api.dto.AiMockInterviewSubmitRequest;
import com.recruitment.api.dto.AiMockInterviewSubmitResponse;
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

    private final MockInterviewService mockInterviewService;

    @PostMapping("/start")
    public Result<AiMockInterviewStartResponse> start(@RequestBody AiMockInterviewStartRequest request) {
        return Result.success(mockInterviewService.start(request));
    }

    @PostMapping("/chat")
    public Result<AiMockInterviewChatResponse> chat(@RequestBody AiMockInterviewChatRequest request) {
        return Result.success(mockInterviewService.chat(request));
    }

    @PostMapping("/submit")
    public Result<AiMockInterviewSubmitResponse> submit(@RequestBody AiMockInterviewSubmitRequest request) {
        return Result.success(mockInterviewService.submit(request));
    }

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
    public Result<AiMockInterviewSubmitResponse> getReport(@PathVariable Long sessionId) {
        return Result.success(mockInterviewService.getReport(sessionId));
    }
}

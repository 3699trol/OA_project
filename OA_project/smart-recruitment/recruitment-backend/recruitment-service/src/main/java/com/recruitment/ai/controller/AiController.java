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
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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

    @PostMapping("/resume/parse-file")
    public Result<AiResumeParseResponse> parseResumeFile(@RequestPart("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error(400, "简历文件不能为空");
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            return Result.error(400, "文件大小不能超过 10MB");
        }
        String fileName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String extension = fileName.contains(".")
                ? fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase()
                : "";
        if (!java.util.Set.of("pdf", "doc", "docx").contains(extension)) {
            return Result.error(400, "仅支持 PDF、DOC、DOCX 简历文件");
        }
        try {
            String content = new Tika().parseToString(file.getInputStream());
            if (!StringUtils.hasText(content)) {
                return Result.error(422, "无法从简历文件中提取文本内容");
            }
            AiResumeParseRequest request = new AiResumeParseRequest();
            request.setResumeContent(content.trim());
            request.setFileType(extension);
            return aiServiceClient.parseResume(request);
        } catch (IOException | TikaException e) {
            return Result.error(422, "简历文件读取失败: " + e.getMessage());
        }
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

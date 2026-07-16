package com.recruitment.ai.matching.controller;

import com.recruitment.api.dto.AiMatchRequest;
import com.recruitment.api.dto.AiMatchResponse;
import com.recruitment.common.core.model.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * AI人岗匹配控制器
 */
@RestController
@RequestMapping("/api/ai/match")
@RequiredArgsConstructor
public class MatchingController {

    @PostMapping
    public Result<AiMatchResponse> match(@RequestBody AiMatchRequest request) {
        // TODO: AI人岗匹配
        return Result.success();
    }
}

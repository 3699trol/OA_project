package com.recruitment.ai.matching.controller;

import com.recruitment.api.dto.AiMatchRequest;
import com.recruitment.api.dto.AiMatchResponse;
import com.recruitment.ai.matching.service.MatchingService;
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

    private final MatchingService matchingService;

    @PostMapping
    public Result<AiMatchResponse> match(@RequestBody AiMatchRequest request) {
        return Result.success(matchingService.match(request));
    }
}

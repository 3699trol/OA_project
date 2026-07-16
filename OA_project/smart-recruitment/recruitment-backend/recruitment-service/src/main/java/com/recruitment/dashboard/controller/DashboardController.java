package com.recruitment.dashboard.controller;

import com.recruitment.common.core.model.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 仪表板控制器
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    @GetMapping("/stats")
    public Result<?> getStats() {
        // TODO: HR统计数据
        return Result.success();
    }
}

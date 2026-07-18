package com.recruitment.system.controller;

import com.recruitment.common.core.model.PageResult;
import com.recruitment.common.core.model.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 操作日志控制器
 */
@RestController
@RequestMapping("/api/system/log")
@RequiredArgsConstructor
public class OperationLogController {

    @GetMapping("/list")
    public Result<PageResult<?>> list(@RequestParam(defaultValue = "1") long page,
                                       @RequestParam(defaultValue = "10") long size,
                                       @RequestParam(required = false) String keyword,
                                       @RequestParam(required = false) String startTime,
                                       @RequestParam(required = false) String endTime) {
        // TODO: 操作日志列表（支持关键字、时间范围筛选）
        return Result.success(PageResult.empty(page, size));
    }
}

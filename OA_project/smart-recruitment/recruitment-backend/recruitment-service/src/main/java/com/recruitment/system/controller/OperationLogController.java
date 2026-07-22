package com.recruitment.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruitment.common.core.model.PageResult;
import com.recruitment.common.core.model.Result;
import com.recruitment.system.entity.OperationLog;
import com.recruitment.system.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 操作日志控制器
 */
@RestController
@RequestMapping("/api/system/log")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class OperationLogController {

    private final OperationLogService operationLogService;

    @GetMapping("/list")
    public Result<PageResult<OperationLog>> list(@RequestParam(name = "page", defaultValue = "1") long page,
                                                  @RequestParam(name = "size", defaultValue = "10") long size,
                                                  @RequestParam(name = "keyword", required = false) String keyword,
                                                   @RequestParam(name = "startTime", required = false) String startTime,
                                                   @RequestParam(name = "endTime", required = false) String endTime) {
        Page<OperationLog> logPage = operationLogService.listByPage(page, size, keyword, startTime, endTime);
        return Result.success(new PageResult<>(logPage.getRecords(), logPage.getTotal(), page, size));
    }
}

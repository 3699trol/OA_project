package com.recruitment.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruitment.common.core.model.PageResult;
import com.recruitment.common.core.model.Result;
import com.recruitment.system.entity.OperationLog;
import com.recruitment.system.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 操作日志控制器
 */
@RestController
@RequestMapping("/api/system/log")
public class OperationLogController {

    @Autowired(required = false)
    private OperationLogService operationLogService;

    @GetMapping("/list")
    public Result<PageResult<OperationLog>> list(@RequestParam(defaultValue = "1") long page,
                                                  @RequestParam(defaultValue = "10") long size,
                                                  @RequestParam(required = false) String keyword,
                                                  @RequestParam(required = false) String startTime,
                                                  @RequestParam(required = false) String endTime) {
        if (operationLogService == null) return Result.success(PageResult.empty(page, size));
        Page<OperationLog> logPage = operationLogService.listByPage(page, size, keyword, startTime, endTime);
        return Result.success(new PageResult<>(logPage.getRecords(), logPage.getTotal(), page, size));
    }
}

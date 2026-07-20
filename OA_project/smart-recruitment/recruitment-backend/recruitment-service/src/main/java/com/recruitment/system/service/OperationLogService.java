package com.recruitment.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruitment.system.entity.OperationLog;

/**
 * 操作日志服务接口
 */
public interface OperationLogService {

    /**
     * 分页查询操作日志，支持关键字、时间范围筛选
     */
    Page<OperationLog> listByPage(long pageNum, long pageSize,
                                   String keyword, String startTime, String endTime);
}

package com.recruitment.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruitment.system.entity.OperationLog;
import com.recruitment.system.mapper.OperationLogMapper;
import com.recruitment.system.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 操作日志服务实现
 */
@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl implements OperationLogService {

    private final OperationLogMapper operationLogMapper;

    @Override
    public Page<OperationLog> listByPage(long pageNum, long pageSize,
                                          String keyword, String startTime, String endTime) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(OperationLog::getModule, keyword)
                    .or()
                    .like(OperationLog::getDescription, keyword)
                    .or()
                    .like(OperationLog::getOperatorName, keyword));
        }
        if (StringUtils.hasText(startTime)) {
            wrapper.ge(OperationLog::getCreateTime, startTime + " 00:00:00");
        }
        if (StringUtils.hasText(endTime)) {
            wrapper.le(OperationLog::getCreateTime, endTime + " 23:59:59");
        }
        wrapper.orderByDesc(OperationLog::getCreateTime);
        return operationLogMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }
}

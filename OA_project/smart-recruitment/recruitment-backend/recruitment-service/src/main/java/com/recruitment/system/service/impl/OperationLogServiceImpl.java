package com.recruitment.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruitment.common.core.exception.BusinessException;
import com.recruitment.system.entity.OperationLog;
import com.recruitment.system.mapper.OperationLogMapper;
import com.recruitment.system.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

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
        validatePagination(pageNum, pageSize);
        LocalDate startDate = parseDate(startTime, "开始时间");
        LocalDate endDate = parseDate(endTime, "结束时间");
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BusinessException(400, "开始时间不能晚于结束时间");
        }

        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String normalizedKeyword = keyword.trim();
            wrapper.and(w -> w
                    .like(OperationLog::getModule, normalizedKeyword)
                    .or()
                    .like(OperationLog::getDescription, normalizedKeyword)
                    .or()
                    .like(OperationLog::getOperatorName, normalizedKeyword));
        }
        if (startDate != null) {
            wrapper.ge(OperationLog::getCreateTime, startDate.atStartOfDay());
        }
        if (endDate != null) {
            wrapper.lt(OperationLog::getCreateTime, endDate.plusDays(1).atStartOfDay());
        }
        wrapper.orderByDesc(OperationLog::getCreateTime)
                .orderByDesc(OperationLog::getId);
        return operationLogMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    private void validatePagination(long pageNum, long pageSize) {
        if (pageNum < 1) {
            throw new BusinessException(400, "页码必须大于等于 1");
        }
        if (pageSize < 1 || pageSize > 100) {
            throw new BusinessException(400, "每页数量必须在 1 到 100 之间");
        }
    }

    private LocalDate parseDate(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException e) {
            throw new BusinessException(400, fieldName + "格式必须为 yyyy-MM-dd");
        }
    }
}

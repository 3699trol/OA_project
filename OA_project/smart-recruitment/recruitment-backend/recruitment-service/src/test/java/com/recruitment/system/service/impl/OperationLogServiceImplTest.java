package com.recruitment.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruitment.common.core.exception.BusinessException;
import com.recruitment.system.entity.OperationLog;
import com.recruitment.system.mapper.OperationLogMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.apache.ibatis.builder.MapperBuilderAssistant;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OperationLogServiceImplTest {

    @Mock
    private OperationLogMapper operationLogMapper;

    private OperationLogServiceImpl operationLogService;

    @BeforeEach
    void setUp() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""), OperationLog.class);
        operationLogService = new OperationLogServiceImpl(operationLogMapper);
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void shouldBuildPagedQueryWithDateRange() {
        when(operationLogMapper.selectPage(any(Page.class), any(Wrapper.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Page<OperationLog> result = operationLogService.listByPage(
                2, 20, "  用户管理  ", "2026-07-01", "2026-07-21");

        assertEquals(2, result.getCurrent());
        assertEquals(20, result.getSize());

        ArgumentCaptor<Wrapper<OperationLog>> captor = ArgumentCaptor.forClass(Wrapper.class);
        verify(operationLogMapper).selectPage(any(Page.class), captor.capture());
        LambdaQueryWrapper<OperationLog> wrapper = (LambdaQueryWrapper<OperationLog>) captor.getValue();
        wrapper.getSqlSegment();
        Collection<Object> values = wrapper.getParamNameValuePairs().values();
        assertTrue(values.contains(LocalDateTime.of(2026, 7, 1, 0, 0)));
        assertTrue(values.contains(LocalDateTime.of(2026, 7, 22, 0, 0)));
    }

    @Test
    void shouldRejectInvalidPagination() {
        BusinessException pageException = assertThrows(BusinessException.class,
                () -> operationLogService.listByPage(0, 10, null, null, null));
        BusinessException sizeException = assertThrows(BusinessException.class,
                () -> operationLogService.listByPage(1, 101, null, null, null));

        assertEquals(400, pageException.getCode());
        assertEquals(400, sizeException.getCode());
        verify(operationLogMapper, never()).selectPage(any(Page.class), any(Wrapper.class));
    }

    @Test
    void shouldRejectInvalidDateFormat() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> operationLogService.listByPage(1, 10, null, "2026/07/01", null));

        assertEquals(400, exception.getCode());
        assertEquals("开始时间格式必须为 yyyy-MM-dd", exception.getMessage());
    }

    @Test
    void shouldRejectReversedDateRange() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> operationLogService.listByPage(
                        1, 10, null, "2026-07-22", "2026-07-01"));

        assertEquals(400, exception.getCode());
        assertEquals("开始时间不能晚于结束时间", exception.getMessage());
    }
}

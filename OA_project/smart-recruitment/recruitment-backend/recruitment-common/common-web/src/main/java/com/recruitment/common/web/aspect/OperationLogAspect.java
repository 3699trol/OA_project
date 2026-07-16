package com.recruitment.common.web.aspect;

import com.recruitment.common.core.context.UserContext;
import com.recruitment.common.web.annotation.OperationLog;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 操作日志切面
 */
@Slf4j
@Aspect
@Component
public class OperationLogAspect {

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint point, OperationLog operationLog) throws Throwable {
        long startTime = System.currentTimeMillis();
        log.info("操作开始 - 模块: {}, 描述: {}, 用户: {}",
                operationLog.module(), operationLog.value(), UserContext.getUsername());

        Object result = point.proceed();

        long costTime = System.currentTimeMillis() - startTime;
        log.info("操作完成 - 模块: {}, 描述: {}, 耗时: {}ms",
                operationLog.module(), operationLog.value(), costTime);

        return result;
    }
}

package com.example.multidatasource.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 数据源切换拦截切面，配合 {@link UsingDataSource}、{@link DynamicRoutingDataSource} 实现数据源切换
 */
@Slf4j
@Aspect
@Component
public class UsingDataSourceAspect {

    @Around("@annotation(usingDataSource)")
    public Object around(ProceedingJoinPoint joinPoint, UsingDataSource usingDataSource) throws Throwable {
        DataSourceType type = usingDataSource.value();
        log.debug("data source type is {}", type);
        // 保存要切换到的数据源类型
        DynamicRoutingDataSource.TYPE_HOLDER.set(type);
        Object res;
        try {
            res = joinPoint.proceed();
        } finally {
            // 恢复数据源
            DynamicRoutingDataSource.TYPE_HOLDER.remove();
        }
        return res;
    }
}

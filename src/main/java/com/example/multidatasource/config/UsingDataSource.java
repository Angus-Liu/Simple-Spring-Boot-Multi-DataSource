package com.example.multidatasource.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 指定使用数据源类型注解，配合 {@link UsingDataSourceAspect}、{@link DynamicDataSource} 实现数据源切换
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UsingDataSource {
    DataSourceType value() default DataSourceType.MASTER;
}

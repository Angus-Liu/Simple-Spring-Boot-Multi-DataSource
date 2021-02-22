package com.example.multidatasource.config;

/**
 * 数据源类型枚举
 */
public enum DataSourceType {
    /**
     * 主库
     */
    MASTER,

    /**
     * 从库 1
     */
    SLAVE,

    /**
     * 从库 2
     */
    SLAVE_2,
    ;

    /**
     * 默认的数据源类型
     */
    public static final DataSourceType DEFAULT = MASTER;
}

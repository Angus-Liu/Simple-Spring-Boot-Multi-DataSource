package com.example.multidatasource.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * 数据源配置
 */
@Configuration
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class DataSourceConfig {

    /**
     * 主库数据源配置，Bean name 与 {@link DataSourceType} 中对应枚举类型名相同，以达到校验作用
     */
    @Bean("MASTER")
    @ConfigurationProperties(prefix = "spring.datasource.master")
    public DataSource masterDataSource() {
        return DataSourceBuilder.create().build();
    }

    /**
     * 从库 1 数据源配置
     */
    @Bean("SLAVE")
    @ConfigurationProperties(prefix = "spring.datasource.slave")
    public DataSource slaveDataSource() {
        return DataSourceBuilder.create().build();
    }

    /**
     * 从库 2 数据源配置
     */
    @Bean("SLAVE_2")
    @ConfigurationProperties(prefix = "spring.datasource.slave2")
    public DataSource slave2DataSource() {
        return DataSourceBuilder.create().build();
    }
}

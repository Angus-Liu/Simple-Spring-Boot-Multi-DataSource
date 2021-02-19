package com.example.multidatasource.config;

import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * 动态数据源配置，配合 {@link UsingDataSource}、{@link UsingDataSourceAspect} 实现数据源切换
 */
@Primary
@Component
public class DynamicDataSource extends AbstractRoutingDataSource {

    private final Map<String, DataSource> dataSourceMap;

    public static final ThreadLocal<DataSourceType> TYPE_HOLDER
            = ThreadLocal.withInitial(DataSourceType::getDefaultValue);

    public DynamicDataSource(Map<String, DataSource> dataSourceMap) {
        this.dataSourceMap = dataSourceMap;
    }

    @Override
    protected Object determineCurrentLookupKey() {
        return TYPE_HOLDER.get();
    }

    @PostConstruct
    private void postConstruct() {
        // 设置默认数据源
        String defaultDataSourceName = DataSourceType.getDefaultValue().name();
        DataSource defaultDataSource = dataSourceMap.get(defaultDataSourceName);
        setDefaultTargetDataSource(defaultDataSource);

        // 设置数据源及其类型映射
        Map<Object, Object> targetDataSources = new HashMap<>();
        dataSourceMap.forEach((name, ds) -> targetDataSources.put(DataSourceType.valueOf(name), ds));
        setTargetDataSources(targetDataSources);
    }
}

# SpringBoot 多数据源 Demo

*SpringBoot Multi-DataSource Demo*

1. 修改 Application，关闭数据源自动配置

```java
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
```

2. 在 application.yml 中添加数据源对应的配置信息

```yml
spring:
  datasource:
    master:
      jdbc-url: jdbc:mysql://localhost:3306/multi_datasource_master
      username: root
      password: rootroot
    slave:
      jdbc-url: jdbc:mysql://localhost:3306/multi_datasource_slave
      username: root
      password: rootroot
    slave2:
      jdbc-url: jdbc:mysql://localhost:3306/multi_datasource_slave
      username: root
      password: rootroot
```

3. 数据源配置

```java
@Configuration
public class DataSourceConfig {

    /**
     * 主库数据源配置，Bean name 须与 DataSourceType 中对应枚举类型名相同
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

```

4. 定义数据源类型枚举

```java
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
     * 获取默认的数据源类型
     */
    public static DataSourceType getDefaultValue() {
        return MASTER;
    }
}
```

5. 动态数据源配置，配合 UsingDataSource、UsingDataSourceAspect 实现数据源切换

```java
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
```

6. 指定使用数据源类型注解，配合 UsingDataSourceAspect、DynamicDataSource 实现数据源切换

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UsingDataSource {
    DataSourceType value() default DataSourceType.MASTER;
}
```

7. 数据源切换拦截切面，配合 UsingDataSource、DynamicDataSource 实现数据源切换

```java
@Slf4j
@Aspect
@Component
public class UsingDataSourceAspect {

    @Around("@annotation(usingDataSource)")
    public Object around(ProceedingJoinPoint joinPoint, UsingDataSource usingDataSource) throws Throwable {
        DataSourceType type = usingDataSource.value();
        log.debug("data source type is {}", type);
        // 保存要切换到的数据源类型
        DynamicDataSource.TYPE_HOLDER.set(type);
        Object res;
        try {
            res = joinPoint.proceed();
        } finally {
            // 恢复数据源
            DynamicDataSource.TYPE_HOLDER.remove();
        }
        return res;
    }
}
```

8. 使用

```java
@Service
public class UserService {

    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User getUser(Integer id) {
        return userDao.selectByPrimaryKey(id);
    }

    @UsingDataSource
    public User getUserFromDefault(Integer id) {
        return userDao.selectByPrimaryKey(id);
    }

    @UsingDataSource(DataSourceType.SLAVE)
    public User getUserFromSlave1(Integer id) {
        return userDao.selectByPrimaryKey(id);
    }

    @UsingDataSource(DataSourceType.SLAVE_2)
    public User getUserFromSlave2(Integer id) {
        return userDao.selectByPrimaryKey(id);
    }
}
```

9. Have fun! 😁

你可以使用 `MultiDatasourceApplicationTests.getUser()` 进行测试
# 简单实现 Spring Boot 多数据源

*Simple Spring Boot Multi-DataSource*

## Usage

```java
// In Mapper
@UsingDataSource(DataSourceType.SLAVE)
User selectByPrimaryKey(Integer id);

// Or In Service
@UsingDataSource(DataSourceType.MASTER)
public User getUserFromSlave1(Integer id) {
  return userDao.selectByPrimaryKey(id);
}

@UsingDataSource(DataSourceType.SLAVE)
public User getUserFromSlave(Integer id) {
  return userDao.selectByPrimaryKey(id);
}
```

## How to

1. 在 application.yml 中添加数据源对应的配置信息

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
         jdbc-url: jdbc:mysql://localhost:3306/multi_datasource_slave_2
         username: root
         password: rootroot
   ```

2. 数据源配置

   ```java
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
   ```

3. 定义数据源类型枚举

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
        * 默认的数据源类型
        */
       public static final DataSourceType DEFAULT = MASTER;
   }
   ```
   
4. 动态数据源配置，配合 UsingDataSource、UsingDataSourceAspect 实现数据源切换

   ```java
   @Primary
   @Component
   public class DynamicRoutingDataSource extends AbstractRoutingDataSource {
   
       private final Map<String, DataSource> dataSourceMap;
   
       public static final ThreadLocal<DataSourceType> TYPE_HOLDER = ThreadLocal.withInitial(() -> DataSourceType.DEFAULT);
   
       public DynamicRoutingDataSource(Map<String, DataSource> dataSourceMap) {
           this.dataSourceMap = dataSourceMap;
       }
   
       @Override
       protected Object determineCurrentLookupKey() {
           return TYPE_HOLDER.get();
       }
   
       @PostConstruct
       private void postConstruct() {
           // 设置默认数据源
           String defaultDataSourceName = DataSourceType.DEFAULT.name();
           DataSource defaultDataSource = dataSourceMap.get(defaultDataSourceName);
           setDefaultTargetDataSource(defaultDataSource);
   
           // 设置数据源及其类型映射
           Map<Object, Object> targetDataSources = new HashMap<>();
           dataSourceMap.forEach((name, ds) -> targetDataSources.put(DataSourceType.valueOf(name), ds));
           setTargetDataSources(targetDataSources);
       }
   }
   ```
   
5. 指定使用数据源类型注解

   ```java
   @Target(ElementType.METHOD)
   @Retention(RetentionPolicy.RUNTIME)
   public @interface UsingDataSource {
       DataSourceType value() default DataSourceType.MASTER;
   }
   ```

6. 数据源切换拦截切面

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
   ```

7. 使用

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
   
8. Have fun! 😁

   你可以使用 `MultiDatasourceApplicationTests.getUser()` 进行测试
   
   ```java
   @SpringBootTest
   class MultiDatasourceApplicationTests {
   
     @Autowired
     private UserService userService;
   
     @Test
     void contextLoads() {
     }
   
     @Test
     public void getUser() {
       User user;
       user = userService.getUser(1);
       System.out.println("user = " + user);
   
       user = userService.getUserFromDefault(1);
       System.out.println("user = " + user);
   
       user = userService.getUserFromSlave1(1);
       System.out.println("user = " + user);
   
       user = userService.getUserFromSlave2(1);
       System.out.println("user = " + user);
   
       user = userService.getUser(1);
       System.out.println("user = " + user);
   
       user = userService.getUserFromDefault(1);
       System.out.println("user = " + user);
   
       user = userService.getUserFromSlave1(1);
       System.out.println("user = " + user);
   
       user = userService.getUserFromSlave2(1);
       System.out.println("user = " + user);
     }
   }
   ```
   
   ```shell
   user = User(id=1, username=master_username, password=master_password, sex=1, create_time=Fri Feb 19 11:59:35 CST 2021, update_time=Fri Feb 19 11:59:35 CST 2021, deleted=0)
   2021-02-20 10:02:12.169  INFO 13053 --- [           main] c.e.m.config.UsingDataSourceAspect       : data source type is MASTER
   user = User(id=1, username=master_username, password=master_password, sex=1, create_time=Fri Feb 19 11:59:35 CST 2021, update_time=Fri Feb 19 11:59:35 CST 2021, deleted=0)
   2021-02-20 10:02:12.171  INFO 13053 --- [           main] c.e.m.config.UsingDataSourceAspect       : data source type is SLAVE
   user = User(id=1, username=slave_username, password=slave_password, sex=1, create_time=Fri Feb 19 17:05:37 CST 2021, update_time=Fri Feb 19 17:05:37 CST 2021, deleted=0)
   2021-02-20 10:02:12.186  INFO 13053 --- [           main] c.e.m.config.UsingDataSourceAspect       : data source type is SLAVE_2
   user = User(id=1, username=slave_2_username, password=slave_2_password, sex=1, create_time=Sat Feb 20 10:00:59 CST 2021, update_time=Sat Feb 20 10:00:59 CST 2021, deleted=0)
   user = User(id=1, username=master_username, password=master_password, sex=1, create_time=Fri Feb 19 11:59:35 CST 2021, update_time=Fri Feb 19 11:59:35 CST 2021, deleted=0)
   2021-02-20 10:02:12.203  INFO 13053 --- [           main] c.e.m.config.UsingDataSourceAspect       : data source type is MASTER
   user = User(id=1, username=master_username, password=master_password, sex=1, create_time=Fri Feb 19 11:59:35 CST 2021, update_time=Fri Feb 19 11:59:35 CST 2021, deleted=0)
   2021-02-20 10:02:12.206  INFO 13053 --- [           main] c.e.m.config.UsingDataSourceAspect       : data source type is SLAVE
   user = User(id=1, username=slave_username, password=slave_password, sex=1, create_time=Fri Feb 19 17:05:37 CST 2021, update_time=Fri Feb 19 17:05:37 CST 2021, deleted=0)
   2021-02-20 10:02:12.208  INFO 13053 --- [           main] c.e.m.config.UsingDataSourceAspect       : data source type is SLAVE_2
   user = User(id=1, username=slave_2_username, password=slave_2_password, sex=1, create_time=Sat Feb 20 10:00:59 CST 2021, update_time=Sat Feb 20 10:00:59 CST 2021, deleted=0)
   ```


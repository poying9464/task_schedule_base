# Task Schedule Base

Task Schedule Base 是一个基于 Quartz 的任务调度框架扩展库，旨在简化任务开发和管理。

## 功能特性

- 提供统一的日志处理功能的 BaseJob 基类
- 自定义注解简化任务和调度配置
- 支持基于注解的自动任务注册
- 自动日志配置（每个任务独立日志文件）
- MDC 支持实现任务日志隔离
- 统一异常处理框架
- 支持 Cron 表达式和简单调度间隔配置

## 安装

### Maven

```xml
<dependency>
    <groupId>org.poying</groupId>
    <artifactId>task-schedule-base</artifactId>
    <version>1.0.0</version>
</dependency>
```

由于本组件将 Quartz 和 SLF4J 的依赖作用域设置为 `provided`，您还需要显式声明这些依赖：

```xml
<dependency>
    <groupId>org.quartz-scheduler</groupId>
    <artifactId>quartz</artifactId>
    <version>2.3.2</version>
</dependency>

<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-api</artifactId>
    <version>2.0.10</version>
</dependency>
```

## Spring Boot 自动配置

本库支持在 Spring Boot 项目中自动配置 Quartz 调度器。要启用自动配置，请在您的 Spring Boot 项目中添加以下依赖：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>
```

然后在 `application.properties` 或 `application.yml` 中配置 Quartz 相关参数：

### application.properties

```properties
# Quartz配置
poying.quartz.scheduler.instance-name=MyQuartzScheduler
poying.quartz.scheduler.instance-id=AUTO

# 线程池配置
poying.quartz.thread-pool.class=org.quartz.simpl.SimpleThreadPool
poying.quartz.thread-pool.thread-count=5
poying.quartz.thread-pool.thread-priority=5

# JobStore配置
poying.quartz.job-store.class=org.quartz.impl.jdbcjobstore.JobStoreTX
poying.quartz.job-store.driver-delegate-class=org.quartz.impl.jdbcjobstore.StdJDBCDelegate
poying.quartz.job-store.table-prefix=QRTZ_
poying.quartz.job-store.clustered=false
poying.quartz.job-store.use-properties=false
poying.quartz.job-store.data-source=myDS

# 数据源配置（可选，如果不配置则使用Spring Boot的spring.datasource配置）
# poying.quartz.data-source.my-ds.driver=com.mysql.cj.jdbc.Driver
# poying.quartz.data-source.my-ds.url=jdbc:mysql://localhost:3306/quartz
# poying.quartz.data-source.my-ds.user=root
# poying.quartz.data-source.my-ds.password=password
# poying.quartz.data-source.my-ds.max-connections=10
# poying.quartz.data-source.my-ds.validation-query=select 1

# Spring数据源配置（用于自动配置）
spring.datasource.url=jdbc:mysql://localhost:3306/quartz
spring.datasource.username=root
spring.datasource.password=password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

### application.yml

```yaml
poying:
  quartz:
    scheduler:
      instance-name: MyQuartzScheduler
      instance-id: AUTO
    thread-pool:
      class: org.quartz.simpl.SimpleThreadPool
      thread-count: 5
      thread-priority: 5
    job-store:
      class: org.quartz.impl.jdbcjobstore.JobStoreTX
      driver-delegate-class: org.quartz.impl.jdbcjobstore.StdJDBCDelegate
      table-prefix: QRTZ_
      clustered: false
      use-properties: false
      data-source: myDS
    # 数据源配置（可选，如果不配置则使用Spring Boot的spring.datasource配置）
    # data-source:
    #   my-ds:
    #     driver: com.mysql.cj.jdbc.Driver
    #     url: jdbc:mysql://localhost:3306/quartz
    #     user: root
    #     password: password
    #     max-connections: 10
    #     validation-query: select 1

# Spring数据源配置（用于自动配置）
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/quartz
    username: root
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

## 使用spring-jdbc和spring-core

本库中的自动配置功能依赖于spring-jdbc和spring-core，使用本库的项目需要提供这些依赖。

如果您使用的是Spring Boot项目，可以通过添加以下依赖来提供这些组件：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>
```

如果您使用的是传统的Spring项目，则需要添加以下依赖：

```xml
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-core</artifactId>
    <version>6.1.11</version>
</dependency>

<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-jdbc</artifactId>
    <version>6.1.11</version>
</dependency>
```

## 使用示例

```java
@ScheduledJob("myJob")
@JobSchedule(cron = "0 0 12 * * ?") // 每天中午12点执行
public class MyJob extends BaseJob {
    @Override
    protected void executeJob(JobExecutionContext context) throws JobExecutionException {
        // 任务逻辑
        System.out.println("执行任务: " + new Date());
    }
}
```

注册任务:

```java
Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
JobRegistrar.registerJob(scheduler, MyJob.class);
scheduler.start();
```

## 核心类说明

### BaseJob

所有任务的基础类，提供日志处理、异常处理等基础功能。

### @ScheduledJob

用于标记任务类的注解，指定任务名称。

### @JobSchedule

用于配置任务调度计划的注解，支持 Cron 表达式和简单调度间隔配置。

### JobRegistrar

注解驱动的任务注册工具，根据注解注册任务到 Quartz 调度器。

## 日志配置

推荐使用 Logback 作为 SLF4J 的实现，以下是一个配置示例：

```xml
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <appender name="TASK_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/task.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/task.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="STDOUT" />
        <appender-ref ref="TASK_FILE" />
    </root>
</configuration>
```

## 构建和部署

构建项目:

```bash
mvn clean package
```

发布到本地仓库:

```bash
mvn clean install
```

发布到远程仓库:

```bash
mvn clean deploy
```

## 许可证

MIT
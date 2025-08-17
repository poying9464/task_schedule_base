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

本库支持在 Spring Boot 项目中自动配置 Quartz 调度器和数据源。要启用自动配置，请在您的 Spring Boot 项目中添加以下依赖：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>

<!-- 如果使用MySQL -->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.33</version>
</dependency>

<!-- 如果使用PostgreSQL -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.7.3</version>
</dependency>
```

然后在 `application.properties` 或 `application.yml` 中配置 Quartz 和数据库连接参数：

### application.properties 示例

```properties
# Quartz配置
poying.quartz.scheduler.instance-name=MyQuartzScheduler
poying.quartz.scheduler.instance-id=AUTO

# 线程池配置
poying.quartz.thread-pool.class=org.quartz.simpl.SimpleThreadPool
poying.quartz.thread-pool.thread-count=5
poying.quartz.thread-pool.thread-priority=5

# JobStore配置（使用JDBC存储）
poying.quartz.job-store.class=org.quartz.impl.jdbcjobstore.JobStoreTX
poying.quartz.job-store.driver-delegate-class=org.quartz.impl.jdbcjobstore.StdJDBCDelegate
poying.quartz.job-store.table-prefix=QRTZ_
poying.quartz.job-store.clustered=false
poying.quartz.job-store.use-properties=false
poying.quartz.job-store.data-source=defaultDS

# Spring数据源配置（自动配置）
spring.datasource.url=jdbc:mysql://localhost:3306/quartz
spring.datasource.username=root
spring.datasource.password=password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.hikari.maximum-pool-size=10
```

### application.yml 示例

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
      data-source: defaultDS

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/quartz
    username: root
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 10
```

### Spring Boot 2.x 用户注意事项

如果您使用的是 Spring Boot 2.x 版本，默认的数据源类型已从 `org.apache.tomcat.jdbc.pool.DataSource` 改为 `com.zaxxer.hikari.HikariDataSource`。请确保配置中包含 HikariCP 依赖：

```xml
<dependency>
    <groupId>com.zaxxer</groupId>
    <artifactId>HikariCP</artifactId>
    <version>5.0.1</version>
</dependency>

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
</dependency>
```

## 配置属性

通过 `QuartzProperties` 类提供了丰富的配置选项，可以更精细地控制 Quartz 调度器行为。以下是支持的配置项：

### QuartzProperties

用于配置 Quartz 调度器的属性：

- `scheduler.instance-name` - 调度器实例名称（默认：自动生成）
- `scheduler.instance-id` - 调度器实例ID（默认：AUTO）
- `thread-pool.class` - 线程池实现类（默认：org.quartz.simpl.SimpleThreadPool）
- `thread-pool.thread-count` - 线程池线程数（默认：10）
- `thread-pool.thread-priority` - 线程优先级（默认：5）
- `job-store.class` - 任务存储实现类（默认：org.quartz.simpl.RAMJobStore）
- `job-store.driver-delegate-class` - JDBC驱动代理类（用于JDBC存储）
- `job-store.table-prefix` - 数据库表前缀（默认：QRTZ_）
- `job-store.clustered` - 是否启用集群（默认：false）
- `job-store.use-properties` - 是否将JobDataMap中的值作为字符串处理（默认：false）
- `job-store.data-source` - 使用的数据源名称（默认：defaultDS）
- `data-source.[name].driver` - 自定义数据源驱动类
- `data-source.[name].url` - 自定义数据源连接URL
- `data-source.[name].user` - 自定义数据源用户名
- `data-source.[name].password` - 自定义数据源密码
- `data-source.[name].max-connections` - 自定义数据源最大连接数
- `data-source.[name].validation-query` - 自定义数据源验证查询

在Spring Boot项目中，可以在application.properties或application.yml中进行配置：

### application.properties 示例

```properties
# 任务调度配置
task.schedule.enabled=true
task.schedule.job-store-type=jdbc
task.schedule.thread-pool-size=10
task.schedule.startup-delay=5
task.schedule.schedule-on-startup=false
task.schedule.overwrite-existing-jobs=false
task.schedule.fail-on-missing-persistent-jobs=true

# 日志配置
task.schedule.task-logging.enabled=true
task.schedule.task-logging.level=DEBUG
task.schedule.task-logging.file-name=detailed-task.log
task.schedule.task-logging.max-history=60
task.schedule.task-logging.rolling-policy=daily

# MDC配置
task.schedule.mdc-context.enabled=true
task.schedule.mdc-context.context-key=quartzTaskId
```

### application.yml 示例

```yaml
task:
  schedule:
    enabled: true
    job-store-type: jdbc
    thread-pool-size: 10
    startup-delay: 5
    schedule-on-startup: false
    overwrite-existing-jobs: false
    fail-on-missing-persistent-jobs: true
    task-logging:
      enabled: true
      level: DEBUG
      file-name: detailed-task.log
      max-history: 60
      rolling-policy: daily
    mdc-context:
      enabled: true
      context-key: quartzTaskId
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

### 2. 注册任务

```java
import org.quartz.Scheduler;

// 获取调度器实例
Scheduler scheduler = ...;

// 注册任务
JobRegistrar.registerAnnotatedJob(scheduler, SampleTask.class);
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

## 自定义注解

### @ScheduledJob

用于标记和配置任务的基本信息：

- `name` - 任务名称
- `group` - 任务组名
- `description` - 任务描述
- `storeDurably` - 是否持久化存储

### @JobSchedule

用于定义任务的调度计划：

- `cron` - Cron 表达式
- `intervalInSeconds` - 简单调度间隔（秒）
- `repeatCount` - 重复次数
- `startNow` - 是否立即启动

## 任务注册工具

[JobRegistrar](file:///D:/develop_pro/coffee/task_schedule/quartz-extensions/src/main/java/org/quartz/extensions/JobRegistrar.java) 提供了基于注解的自动任务注册功能：

```java
// 注册单个任务
JobRegistrar.registerAnnotatedJob(scheduler, YourJobClass.class);
```

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

## 构建和发布

### 构建项目

```bash
mvn clean package
```

### 发布到本地仓库

```bash
mvn clean install
```

### 发布到远程仓库

```bash
mvn clean deploy
```

## 许可证

MIT
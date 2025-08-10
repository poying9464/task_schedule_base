# Task Schedule Base

Task Schedule Base 是一个为 task_schedule 调度框架提供的扩展库，包含基础任务类、自定义注解和任务注册工具，旨在简化添加task_schedule的任务管理。

## 功能特性

1. **BaseJob 基类** - 提供统一的日志处理功能
2. **自定义注解** - 简化任务和调度配置
3. **任务注册工具** - 支持基于注解的自动任务注册

## 安装

在 Maven 项目中添加以下依赖：

```xml
<dependency>
    <groupId>org.poying</groupId>
    <artifactId>quartz-extensions</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 使用方法

### 1. 创建任务类

```java
import org.poying.base.BaseJob;
import annotations.org.poying.base.ScheduledJob;
import annotations.org.poying.base.JobSchedule;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@ScheduledJob(
        name = "SampleTask",
        group = "SAMPLE_GROUP",
        description = "这是一个示例任务",
        taskId = "202518221236"
)
@JobSchedule(
        cron = "0 0/5 * * * ?",
        startNow = true
)
public class SampleTask extends BaseJob {

    @Override
    protected void executeJob(JobExecutionContext context) throws JobExecutionException {
        logger.info("执行示例任务");
        // 在这里实现具体的业务逻辑
    }
}
```

### 2. 注册任务

```java

import org.quartz.Scheduler;

// 获取调度器实例
Scheduler scheduler = ...;

// 注册任务
        JobRegistrar.

        registerAnnotatedJob(scheduler, SampleTask .class);
```

## BaseJob 基类

[BaseJob](file:///D:/develop_pro/coffee/task_schedule/quartz-extensions/src/main/java/org/quartz/extensions/BaseJob.java) 是所有任务的基类，提供了以下功能：

1. **自动日志配置** - 每个任务会自动获得独立的日志文件
2. **MDC 支持** - 通过 MDC 机制实现任务日志隔离
3. **统一异常处理** - 提供基础的异常处理框架

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

任务会自动获得独立的日志文件，文件命名规则为：
- `task_schedule_任务类名.log` - 当前日志文件
- `task_schedule_任务类名.yyyy-MM-dd.i.log` - 历史日志文件

要启用此功能，需要在 logback 配置中添加相应的 appender 配置：

```xml
<appender name="TASK_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>logs/task_schedule_%mdc{jobName:-default}.log</file>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
        <fileNamePattern>logs/task_schedule_%mdc{jobName:-default}.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
        <maxFileSize>100MB</maxFileSize>
        <maxHistory>30</maxHistory>
    </rollingPolicy>
    <encoder>
        <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
    </encoder>
</appender>

<logger name="your.job.package" level="INFO" additivity="false">
    <appender-ref ref="TASK_FILE"/>
</logger>
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

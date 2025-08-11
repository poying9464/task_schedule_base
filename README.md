# Task Schedule Base

Task Schedule Base is an extension library for the task_schedule framework, including base job classes, custom annotations, and job registration tools, designed to simplify task management in task_schedule.

## Features

1. **BaseJob Class** - Provides unified logging functionality
2. **Custom Annotations** - Simplifies job and scheduling configuration
3. **Job Registration Tool** - Supports annotation-based automatic job registration

## Installation

Add the following dependency to your Maven project:

```xml
<dependency>
    <groupId>org.poying</groupId>
    <artifactId>task-schedule-base</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Usage

### 1. Create a Job Class

```java
import org.poying.base.job.BaseJob;
import org.poying.base.ann.ScheduledJob;
import org.poying.base.ann.JobSchedule;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@ScheduledJob(
        name = "SampleTask",
        group = "SAMPLE_GROUP",
        description = "This is a sample task",
        taskId = "202518221236"
)
@JobSchedule(
        cron = "0 0/5 * * * ?",
        startNow = true
)
public class SampleTask extends BaseJob {

    @Override
    protected void executeJob(JobExecutionContext context) throws JobExecutionException {
        logger.info("Executing sample task");
        // Implement your specific business logic here
    }
}
```

### 2. Register the Job

```java
import org.quartz.Scheduler;

// Get scheduler instance
Scheduler scheduler = ...;

// Register job
JobRegistrar.registerAnnotatedJob(scheduler, SampleTask.class);
```

## BaseJob Class

[BaseJob](file:///D:/develop_pro/coffee/task_schedule/quartz-extensions/src/main/java/org/quartz/extensions/BaseJob.java) is the base class for all jobs, providing the following features:

1. **Automatic Log Configuration** - Each job automatically gets its own log file
2. **MDC Support** - Task log isolation through MDC mechanism
3. **Unified Exception Handling** - Provides basic exception handling framework

## Custom Annotations

### @ScheduledJob

Used to mark and configure basic job information:

- `name` - Job name
- `group` - Job group name
- `description` - Job description
- `storeDurably` - Whether to store persistently

### @JobSchedule

Used to define the job scheduling plan:

- `cron` - Cron expression
- `intervalInSeconds` - Simple scheduling interval (seconds)
- `repeatCount` - Number of repetitions
- `startNow` - Whether to start immediately

## Job Registration Tool

[JobRegistrar](file:///D:/develop_pro/coffee/task_schedule/quartz-extensions/src/main/java/org/quartz/extensions/JobRegistrar.java) provides annotation-based automatic job registration functionality:

```java
// Register a single job
JobRegistrar.registerAnnotatedJob(scheduler, YourJobClass.class);
```

## Logging Configuration

Jobs automatically get independent log files with the following naming convention:
- `task_schedule_JobClassName.log` - Current log file
- `task_schedule_JobClassName.yyyy-MM-dd.i.log` - Historical log files

To enable this feature, add the corresponding appender configuration in your logback configuration:

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

## Build and Deployment

### Build Project

```bash
mvn clean package
```

### Deploy to Local Repository

```bash
mvn clean install
```

### Deploy to Remote Repository

```bash
mvn clean deploy
```

## License

MIT
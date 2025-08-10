package org.poying.base;

import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.poying.base.annotations.ScheduledJob;
import org.poying.base.annotations.JobSchedule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * 任务注册工具类
 * 提供基于注解的任务自动注册功能
 */
public class JobRegistrar {
    
    private static final Logger logger = LoggerFactory.getLogger(JobRegistrar.class);
    
    /**
     * 注册基于注解的任务
     * 
     * @param scheduler 调度器
     * @param jobClass 任务类
     * @throws SchedulerException 调度器异常
     */
    public static void registerAnnotatedJob(Scheduler scheduler, Class<? extends Job> jobClass) 
            throws SchedulerException {
        
        // 检查是否是BaseJob的子类
        if (!BaseJob.class.isAssignableFrom(jobClass)) {
            throw new IllegalArgumentException("任务类必须继承BaseJob");
        }
        
        // 获取注解信息
        ScheduledJob scheduledJob = jobClass.getAnnotation(ScheduledJob.class);
        JobSchedule jobSchedule = jobClass.getAnnotation(JobSchedule.class);
        
        // 构建JobDetail
        JobDetail jobDetail = buildJobDetail(jobClass, scheduledJob);
        
        // 添加任务到调度器
        scheduler.addJob(jobDetail, true);
        logger.info("成功注册任务: {}", jobDetail.getKey());
        
        // 如果有调度注解，创建并注册触发器
        if (jobSchedule != null) {
            Trigger trigger = buildTrigger(jobDetail, jobSchedule);
            if (scheduler.getTrigger(trigger.getKey()) == null) {
                scheduler.scheduleJob(trigger);
                logger.info("成功注册触发器: {}", trigger.getKey());
            } else {
                logger.info("触发器已存在: {}", trigger.getKey());
            }
        }
    }
    
    /**
     * 构建JobDetail
     */
    private static JobDetail buildJobDetail(Class<? extends Job> jobClass, ScheduledJob scheduledJob) {
        JobBuilder jobBuilder = JobBuilder.newJob(jobClass);
        
        if (scheduledJob != null) {
            // 设置任务名称和组
            String name = scheduledJob.name();
            if (name.isEmpty()) {
                name = jobClass.getSimpleName();
            }
            
            jobBuilder
                .withIdentity(name, scheduledJob.group())
                .withDescription(scheduledJob.description())
                .storeDurably(scheduledJob.storeDurably());
        } else {
            // 默认使用类名作为任务名
            jobBuilder
                .withIdentity(jobClass.getSimpleName())
                .storeDurably(true);
        }
        
        return jobBuilder.build();
    }
    
    /**
     * 构建Trigger
     */
    private static Trigger buildTrigger(JobDetail jobDetail, JobSchedule jobSchedule) {
        TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(jobDetail.getKey().getName() + "Trigger", jobDetail.getKey().getGroup());
        
        if (jobSchedule.startNow()) {
            triggerBuilder.startNow();
        }
        
        // 根据配置创建不同类型的触发器
        if (!jobSchedule.cron().isEmpty()) {
            triggerBuilder.withSchedule(org.quartz.CronScheduleBuilder.cronSchedule(jobSchedule.cron()));
        } else if (jobSchedule.intervalInSeconds() > 0) {
            org.quartz.SimpleScheduleBuilder scheduleBuilder = org.quartz.SimpleScheduleBuilder
                    .simpleSchedule()
                    .withIntervalInSeconds((int) jobSchedule.intervalInSeconds());
            
            if (jobSchedule.repeatCount() >= 0) {
                scheduleBuilder.withRepeatCount(jobSchedule.repeatCount());
            } else {
                scheduleBuilder.repeatForever();
            }
            
            triggerBuilder.withSchedule(scheduleBuilder);
        }
        
        return triggerBuilder.build();
    }
}
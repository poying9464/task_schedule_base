package org.poying.base.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于定义任务调度计划的注解
 * 可以指定cron表达式或简单调度参数
 *
 * @author poying
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface JobSchedule {



    /**
     * Cron表达式
     * @return cron表达式
     */
    String cron() default "";
    
    /**
     * 简单调度间隔（秒）
     * @return 间隔秒数
     */
    long intervalInSeconds() default 0;
    
    /**
     * 重复次数
     * -1表示无限重复
     * @return 重复次数
     */
    int repeatCount() default -1;
    
    /**
     * 是否立即启动
     * @return 是否立即启动
     */
    boolean startNow() default true;
}
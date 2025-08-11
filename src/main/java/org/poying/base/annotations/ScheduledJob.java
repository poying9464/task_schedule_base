package org.poying.base.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于标记Quartz任务的注解
 * 可以定义任务的名称、描述、组等元信息
 *
 * @author poying
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ScheduledJob {

    /**
     * 任务id
     * @return 任务id
     */
    String taskId() default "";

    /**
     * 任务名称
     * @return 任务名称
     */
    String name() default "";
    
    /**
     * 任务描述
     * @return 任务描述
     */
    String description() default "";
    
    /**
     * 任务组名
     * @return 任务组名
     */
    String group() default "DEFAULT";
    
    /**
     * 是否持久化存储
     * @return 是否持久化
     */
    boolean storeDurably() default true;
}
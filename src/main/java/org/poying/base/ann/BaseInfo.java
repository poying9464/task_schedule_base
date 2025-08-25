package org.poying.base.ann;

import org.poying.base.job.BaseJob;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于标记任务的基本信息
 *
 * @author poying
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BaseInfo {

    /**
     * 依赖的任务
     * 
     * @return 依赖的任务数组
     */
    Class<? extends BaseJob>[] dependOn() default {};

    /**
     * 依赖的任务组
     * 
     * @return 依赖的任务组数组
     */
    String[] dependOnGroup() default {};


}
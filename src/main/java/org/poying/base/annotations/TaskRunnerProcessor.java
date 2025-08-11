package org.poying.base.annotations;

import org.poying.base.ext.Surround;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 任务执行器注解
 * 用于注解定时任务增强的配置类
 *
 * @author poying
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface TaskRunnerProcessor {

    Class<? extends Surround>[] surrounds() default {};
}
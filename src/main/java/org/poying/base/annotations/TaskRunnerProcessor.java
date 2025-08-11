package org.poying.base.annotations;

import org.poying.base.ext.Surround;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 任务运行器处理器注解
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface TaskRunnerProcessor {

    /**
     * 任务环绕处理器类数组
     * 
     * @return Surround实现类数组
     */
    Class<? extends Surround>[] surrounds() default {};
}
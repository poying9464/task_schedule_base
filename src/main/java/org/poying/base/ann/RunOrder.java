package org.poying.base.ann;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 指定执行的顺序
 *
 * @author panyu
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RunOrder {

    /**
     * before阶段的执行顺序
     * 
     * @return 执行顺序值
     */
    int before() default 0;
    
    /**
     * after阶段的执行顺序
     * 
     * @return 执行顺序值
     */
    int after() default 0;

}
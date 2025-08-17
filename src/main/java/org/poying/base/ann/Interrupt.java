package org.poying.base.ann;

import org.poying.base.ext.pyext.DefaultInterruptHandler;
import org.poying.base.ext.InterruptHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用来引进处理中斷异常的处理类
 *
 * @author poying
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Interrupt {

    Class<? extends InterruptHandler> handler() default DefaultInterruptHandler.class;

}

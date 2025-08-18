package org.poying.base.ann;

import org.poying.base.ext.ExceptionHandler;
import org.poying.base.ext.pyext.DefaultExceptionHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExceptionCapture {

    Class<? extends Exception>[] value() default Exception.class;

    Class<? extends ExceptionHandler> handler() default DefaultExceptionHandler.class;

}

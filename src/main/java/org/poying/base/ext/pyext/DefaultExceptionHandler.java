package org.poying.base.ext.pyext;

import org.poying.base.ext.ExceptionHandler;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 默认异常处理
 *
 * @author poying
 */
public class DefaultExceptionHandler implements ExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(DefaultExceptionHandler.class);

    @Override
    public void handle(Exception e, JobExecutionContext context) {
        log.error("Job exception", e);
    }
}

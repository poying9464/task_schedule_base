package org.poying.base.ext.pyext;

import org.poying.base.ext.InterruptHandler;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultInterruptHandler implements InterruptHandler {

    private static final Logger log = LoggerFactory.getLogger(DefaultInterruptHandler.class);

    @Override
    public void handle(JobExecutionContext context, Throwable throwable) {
        // print stack trace
        log.error("Job interrupted", throwable);
    }
}

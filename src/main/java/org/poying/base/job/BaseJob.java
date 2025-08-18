package org.poying.base.job;

import org.quartz.JobExecutionContext;

/**
 * BaseJob
 *
 * @author poying
 */
public abstract class BaseJob extends PyTask {

    @Override
    public void afterInit(JobExecutionContext context) {

    }

    @Override
    public void afterFinally(JobExecutionContext context) {

    }
}

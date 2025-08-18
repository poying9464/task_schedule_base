package org.poying.base.job;

import org.quartz.JobExecutionContext;

/**
 * BaseJob
 *
 * @author poying
 */
public abstract class BaseJob extends PyTask {

    @Override
    public final void afterInit(JobExecutionContext context) {
        // 默认实现，防止子类重写此方法
    }

    @Override
    public final void afterFinally(JobExecutionContext context) {
        // 默认实现，防止子类重写此方法
    }
}
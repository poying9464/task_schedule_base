package org.poying.base.ext;

import org.quartz.JobExecutionContext;

public class TaskDependSurround implements Surround {

    @Override
    public boolean before(JobExecutionContext context) {
        return false;
    }

    @Override
    public boolean after(JobExecutionContext context) {
        return true;
    }

    @Override
    public void integration(JobExecutionContext context) {

    }
}

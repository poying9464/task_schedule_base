package org.poying.base.ext.pyext;

import org.poying.base.db.TaskMysqlDao;
import org.poying.base.ext.Surround;
import org.quartz.JobExecutionContext;

public class TaskDependSurround implements Surround {

    @Override
    public final boolean before(JobExecutionContext context) {
        String jobKey = context.getJobDetail().getKey().toString();
        String taskName = context.getJobDetail().getKey().getName();
        return TaskMysqlDao.isSuccessful(jobKey, taskName);
    }

    @Override
    public final boolean after(JobExecutionContext context) {
        return true;
    }

    @Override
    public final void integration(JobExecutionContext context) {

    }
}

package org.poying.base.ext.pyext;

import org.poying.base.ann.BaseInfo;
import org.poying.base.db.TaskMysqlDao;
import org.poying.base.ext.Surround;
import org.poying.base.job.BaseJob;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

public class TaskDependSurround implements Surround {

    @Override
    public final boolean before(JobExecutionContext context) {
        String jobKey = context.getJobDetail().getKey().toString();
        String taskName = context.getJobDetail().getKey().getName();
        Class<? extends Job> jobClass = context.getJobDetail().getJobClass();
        // 修复：检查jobClass是否继承自BaseJob
        if (BaseJob.class.isAssignableFrom(jobClass)) {
            // 获取注解
            BaseInfo annotation = jobClass.getAnnotation(BaseInfo.class);
            if (annotation != null) {
                String[] dependOnGroup = annotation.dependOnGroup();
                for (String group : dependOnGroup) {
                    if (!TaskMysqlDao.groupIsSuccessful(group)) {
                        return false;
                    }
                }
                for (Class<? extends Job> dependOn : annotation.dependOn()) {
                    if (!TaskMysqlDao.isSuccessful(dependOn.getSimpleName(), dependOn.getSimpleName())) {
                        return false;
                    }
                }
            }
        }
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

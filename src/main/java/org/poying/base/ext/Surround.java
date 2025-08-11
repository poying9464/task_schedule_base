package org.poying.base.ext;

import org.quartz.JobExecutionContext;

/**
 * 任务运行时围绕扩展点运行，提供扩展点
 */

public interface Surround {

    /**
     * 任务运行前运行
     */
    void before(JobExecutionContext context);

    /**
     * 任务运行后运行
     */
    void after(JobExecutionContext context);

}

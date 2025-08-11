package org.poying.base.ext;

import org.quartz.JobExecutionContext;

/**
 * 中断处理接口
 *
 * @author poying
 */
public interface InterruptHandler {

    /**
     * 处理中断
     * @param context 任务执行上下文
     * @param throwable 中断异常
     */
    void handle(JobExecutionContext context, Throwable throwable);

}

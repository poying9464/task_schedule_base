package org.poying.base.ext;

import org.quartz.JobExecutionContext;

/**
 * 异常处理接口
 *
 * @author poying
 */
public interface ExceptionHandler {
    /**
     * 处理异常
     *
     * @param e       异常
     * @param context 任务执行上下文
     */
    void handle(Exception e, JobExecutionContext context);

}

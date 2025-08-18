package org.poying.base.job;

import org.quartz.JobExecutionContext;

/**
 *
 * 所有任务的基类，提供统一的日志处理功能
 * 通过MDC机制实现每个任务单独的日志文件。
 *
 * <p>使用示例：</p>
 * <pre>
 * public class MyJob extends BaseJob {
 *   {@literal @}Override
 *    protected void executeJob(JobExecutionContext context) {
 *      logger.info("Executing job");
 *         // 任务逻辑在这里
 *      }
 *  }
 *  </pre>
 *  <p>
 * 日志文件会自动命名为task_schedule_任务类名.log
 * </p>
 *
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
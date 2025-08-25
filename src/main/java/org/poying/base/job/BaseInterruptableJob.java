package org.poying.base.job;

import org.poying.base.ann.Interrupt;
import org.poying.base.ext.InterruptHandler;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.UnableToInterruptJobException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;

/**
 * 可中断任务的基类，提供统一的日志处理功能和中断处理机制
 * 通过MDC机制实现每个任务单独的日志文件。
 *
 * <p>使用示例：</p>
 * <pre>
 * public class MyJob extends BaseInterruptableJob {
 *     {@literal @}Override
 *     protected void executeJob(JobExecutionContext context) {
 *         logger.info("Executing job");
 *         // 任务逻辑在这里
 *     }
 * }
 * </pre>
 * <p>
 * 日志文件会自动命名为task_schedule_任务类名.log
 * </p>
 *
 * @author poying
 */
public abstract class BaseInterruptableJob extends PyJob implements InterruptableJob {

    /**
     * 日志记录器
     */
    protected Logger logger;

    // 使用ThreadLocal保存执行上下文，以便在interrupt方法中使用
    private static final ThreadLocal<JobExecutionContext> contextHolder = new ThreadLocal<>();

    /**
     * 构造函数，初始化日志记录器。
     * 使用子类的实际类型作为日志名称
     */
    public BaseInterruptableJob() {
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    @Override
    public void afterInit(JobExecutionContext context) {
        contextHolder.set(context);
    }

    @Override
    public void afterFinally(JobExecutionContext context) {
        contextHolder.remove();
    }

    @Override
    public void interrupt() throws UnableToInterruptJobException {
        Interrupt annotation = this.getClass().getAnnotation(Interrupt.class);
        if (annotation != null) {
            Class<? extends InterruptHandler> handler = annotation.handler();
            try {
                Constructor<? extends InterruptHandler> declaredConstructor = handler.getDeclaredConstructor();
                InterruptHandler instance = declaredConstructor.newInstance();
                // 传递上下文和异常信息到handle方法
                JobExecutionContext context = contextHolder.get();
                if (context != null) {
                    instance.handle(context, new UnableToInterruptJobException("Job interrupted"));
                }
            } catch (Exception e) {
                logger.error("InterruptHandler's class must have an constructor without param", e);
            }
        }
    }

}
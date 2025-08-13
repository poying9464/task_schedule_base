package org.poying.base.job;

import org.poying.base.e.SurroundWithOrder;
import org.poying.base.ext.Surround;
import org.poying.base.ann.RunOrder;
import org.poying.base.ann.TaskRunnerProcessor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 所有任务的基类，提供统一的日志处理功能
 * 通过MDC机制实现每个任务单独的日志文件。
 *
 * <p>使用示例：</p>
 * <pre>
 * public class MyJob extends BaseJob {
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
public abstract class BaseJob implements Job {

    /**
     * 日志记录器
     */
    protected Logger logger;

    /**
     * 构造函数，初始化日志记录器。
     * 使用子类的实际类型作为日志名称
     */
    public BaseJob() {
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    @Override
    public final void execute(JobExecutionContext context) throws JobExecutionException {
        // 从任务类名中提取任务名称，用于日志文件命名
        String jobName = this.getClass().getSimpleName();
        MDC.put("jobName", jobName);
        context.put("task_schedule_job_name_$9527", jobName);

        try {
            before(context);
            try {
                // 调用具体的任务执行逻辑
                executeJob(context);
            } finally {
                // 清理MDC上下文前先执行after方法
                after(context);
            }
        } finally {
            // 最后清理MDC上下文
            integration(context);
            MDC.clear();
        }
    }

    /**
     * 整合before和after逻辑
     *
     * @param context ctx
     */
    private void integration(JobExecutionContext context) {
        List<SurroundWithOrder> surrounds = getAscendingOrderSurrounds();
        // 创建一个新的列表用于排序，避免修改原始列表

        for (SurroundWithOrder surroundWithOrder : surrounds) {
            try {
                surroundWithOrder.surround().integration(context);
            } catch (Exception ignore) {
            }
        }
    }

    /**
     * 任务执行完成后的逻辑增强
     *
     * @param context ctx
     */
    private void after(JobExecutionContext context) {
        List<SurroundWithOrder> surrounds = getAscendingOrderSurrounds();
        // 创建一个新的列表用于排序，避免修改原始列表

        for (SurroundWithOrder surroundWithOrder : surrounds) {
            try {
                surroundWithOrder.surround().after(context);
            } catch (Exception ignore) {
            }
        }
    }

    /**
     * 任务执行前的逻辑增强
     *
     * @param context ctx
     */
    private void before(JobExecutionContext context) {
        List<SurroundWithOrder> surrounds = getAscendingOrderSurrounds();
        // 创建一个新的列表用于排序，避免修改原始列表

        for (SurroundWithOrder surroundWithOrder : surrounds) {
            try {
                surroundWithOrder.surround().before(context);
            } catch (Exception ignore) {
            }
        }
    }

    /**
     * 从@TaskRunnerProcessor注解中获取Surround实例列表，并包装为带排序信息的对象
     *
     * @return 带排序信息的Surround包装列表
     */
    private List<SurroundWithOrder> getAscendingOrderSurrounds() {
        List<Surround> surrounds = new ArrayList<>();
        TaskRunnerProcessor taskRunnerProcessor = this.getClass().getAnnotation(TaskRunnerProcessor.class);

        if (taskRunnerProcessor != null) {
            Class<? extends Surround>[] surroundClasses = taskRunnerProcessor.surrounds();
            for (Class<? extends Surround> surroundClass : surroundClasses) {
                try {
                    surrounds.add(surroundClass.getDeclaredConstructor().newInstance());
                } catch (Exception e) {
                    logger.error("Unable to instantiated Surround class: {}", surroundClass.getName(), e);
                }
            }
        }
        List<SurroundWithOrder> surroundsWithOrder = new ArrayList<>();
        for (int i = 0; i < surrounds.size(); i++) {
            Surround surround = surrounds.get(i);
            int order = i; // 默认使用索引作为顺序值，避免Integer.MAX_VALUE问题
            RunOrder runOrder = surround.getClass().getAnnotation(RunOrder.class);
            if (runOrder != null) {
                order = runOrder.before();
            }
            surroundsWithOrder.add(new SurroundWithOrder(surround, order));
        }

        // 按照order值升序排序
        surroundsWithOrder.sort(Comparator.comparingInt(SurroundWithOrder::order));
        return surroundsWithOrder;
    }

    /**
     * 具体的任务执行逻辑，由子类实现
     *
     * @param context 任务执行上下文
     * @throws JobExecutionException 任务执行异常
     */
    protected abstract void executeJob(JobExecutionContext context) throws JobExecutionException;


}
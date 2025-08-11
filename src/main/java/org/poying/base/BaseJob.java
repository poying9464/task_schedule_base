package org.poying.base;

import org.poying.base.ext.Surround;
import org.poying.base.annotations.RunOrder;
import org.poying.base.annotations.TaskRunnerProcessor;
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
 * 通过MDC机制实现每个任务单独的日志文件
 * <p>
 * 使用方法：
 * 1. 继承BaseJob类
 * 2. 实现executeJob方法编写具体任务逻辑
 * 3. 任务会自动获得独立的日志文件，文件名为task_schedule_任务类名.log
 */
public abstract class BaseJob implements Job {

    protected Logger logger;

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
            surroundWithOrder.surround.integration(context);
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
            surroundWithOrder.surround.after(context);
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
            surroundWithOrder.surround.before(context);
        }
    }

    /**
     * 从@TaskRunnerProcessor注解中获取Surround列表
     *
     * @return Surround列表
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
                    logger.error("无法实例化Surround类: {}", surroundClass.getName(), e);
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
        surroundsWithOrder.sort(Comparator.comparingInt(o -> o.order));
        return surroundsWithOrder;
    }

    /**
     * 具体的任务执行逻辑，由子类实现
     *
     * @param context 任务执行上下文
     * @throws JobExecutionException 任务执行异常
     */
    protected abstract void executeJob(JobExecutionContext context) throws JobExecutionException;


    /**
     * 用于包装Surround和其执行顺序的内部类
     */
    private record SurroundWithOrder(Surround surround, int order) {
    }
}
package org.poying.base.job;

import org.poying.base.ann.ExceptionCapture;
import org.poying.base.e.SurroundWithOrder;
import org.poying.base.ext.ExceptionHandler;
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
import java.util.BitSet;
import java.util.Comparator;
import java.util.List;

/**
 * 任务抽象类，继承自Quartz的Job接口。
 *
 * @author poying
 */
abstract class PyJob implements Job {

    /**
     * 日志记录器
     */
    protected Logger logger;

    /**
     * 构造函数，初始化日志记录器。
     * 使用子类的实际类型作为日志名称
     */
    public PyJob() {
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    @Override
    public final void execute(JobExecutionContext context) throws JobExecutionException {
        // 从任务类名中提取任务名称，用于日志文件命名
        init(context);
        try {
            if (beforeExecute(context)) {
                try {
                    // 调用具体的任务执行逻辑
                    executeJob(context);
                } catch (Exception e) {
                    onException(e, context);
                }
            }
            // 清理MDC上下文前先执行after方法
            afterExecute(context);
        } finally {
            // 最后清理MDC上下文
            onFinally(context);
        }
    }

    /**
     * 初始化
     *
     * @param context 任务执行上下文
     */
    private void init(JobExecutionContext context) {
        String jobName = this.getClass().getSimpleName();
        MDC.put("jobName", jobName);
        context.put("task_schedule_job_name_$9527", jobName);
        afterInit(context);
    }

    /**
     * 初始化后的逻辑
     *
     * @param context 任务执行上下文
     */
    public abstract void afterInit(JobExecutionContext context);

    /**
     * 任务执行完成后的逻辑
     *
     * @param context 任务执行上下文
     */
    private void onFinally(JobExecutionContext context) {
        integration(context);
        MDC.clear();
        afterFinally(context);
    }

    /**
     * 任务执行完成后的逻辑
     *
     * @param context 任务执行上下文
     */
    public abstract void afterFinally(JobExecutionContext context);

    /**
     * 异常处理逻辑
     *
     * @param e       异常
     * @param context 任务执行上下文
     */
    private void onException(Exception e, JobExecutionContext context) {
        ExceptionCapture capture = this.getClass().getAnnotation(ExceptionCapture.class);
        if (capture != null) {
            for (Class<? extends Exception> exceptionClass : capture.value()) {
                Class<? extends ExceptionHandler> handler = capture.handler();
                if (exceptionClass.isAssignableFrom(e.getClass())) {
                    try {
                        if (handler != null) {
                            handler.getDeclaredConstructor().newInstance().handle(e, context);
                        }
                    } catch (Exception ex) {
                        logger.error("Unable to instantiated ExceptionHandler class: {}", handler.getName(), ex);
                    }
                }
            }
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
    private boolean afterExecute(JobExecutionContext context) {
        List<SurroundWithOrder> surrounds = getAscendingOrderSurrounds();

        BitSet results = new BitSet(surrounds.size());
        for (int i = 0; i < surrounds.size(); i++) {
            SurroundWithOrder surroundWithOrder = surrounds.get(i);
            try {
                boolean result = surroundWithOrder.surround().after(context);
                results.set(i, result);
            } catch (Exception e) {
                logger.warn("Exception occurred in surround: {}",
                        surroundWithOrder.surround().getClass().getSimpleName(), e);
                // 异常时默认设置为true
                results.set(i, true);
            }
        }
        // 检查是否所有位都被设置为true
        return results.cardinality() == surrounds.size();
    }

    /**
     * 任务执行前的逻辑增强
     *
     * @param context ctx
     */
    private boolean beforeExecute(JobExecutionContext context) {
        List<SurroundWithOrder> surrounds = getAscendingOrderSurrounds();
        BitSet results = new BitSet(surrounds.size());

        for (int i = 0; i < surrounds.size(); i++) {
            SurroundWithOrder surroundWithOrder = surrounds.get(i);
            try {
                boolean result = surroundWithOrder.surround().before(context);
                results.set(i, result);
            } catch (Exception e) {
                logger.warn("Exception occurred in surround: {}",
                        surroundWithOrder.surround().getClass().getSimpleName(), e);
                // 异常时默认设置为true
                results.set(i, true);
            }
        }

        // 检查是否所有位都被设置为true
        return results.cardinality() == surrounds.size();
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
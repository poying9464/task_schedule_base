package org.poying.base.ext;

import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 任务运行时围绕扩展点运行，提供扩展点
 */
public interface Surround {

    /**
     * Logger实例，实现类可以通过 getLogger() 方法获取
     */
    Logger log = LoggerFactory.getLogger(Surround.class);

    /**
     * 任务运行前运行
     */
    void before(JobExecutionContext context);

    /**
     * 任务运行后运行
     */
    void after(JobExecutionContext context);

    /**
     * 在{@link this#before}和{@link this#after}都执行完之后运行
     *
     */
    void integration(JobExecutionContext context);

    /**
     * 获取Logger实例的方法，实现类可以覆盖此方法以获取自己的Logger
     *
     * @return Logger实例
     */
    default Logger getLogger() {
        return log;
    }
}
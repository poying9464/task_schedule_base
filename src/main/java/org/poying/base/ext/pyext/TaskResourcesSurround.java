package org.poying.base.ext.pyext;

import org.poying.base.ann.RunOrder;
import org.poying.base.db.TaskMysqlDao;
import org.poying.base.ext.Surround;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 任务资源使用统计
 *
 * @author poying
 */
@RunOrder(after = -1, before = -1)
public class TaskResourcesSurround implements Surround, JobListener {

    private static final Logger logger = LoggerFactory.getLogger(TaskResourcesSurround.class);

    public static final String LISTENER_NAME = "TaskResourcesSurround";

    // 存储任务监控实例的线程安全Map（用于Quartz监听器方式）
    private final Map<String, TaskMonitor> monitorMap = new ConcurrentHashMap<>();

    // 用于任务增强类方式的线程本地变量
    private final ThreadLocal<TaskMonitor> threadLocalMonitor = new ThreadLocal<>();

    private TaskResourceInfo taskResourceInfo;


    @Override
    public String getName() {
        return LISTENER_NAME;
    }

    @Override
    public final boolean before(JobExecutionContext context) {
        String jobKey = context.getJobDetail().getKey().toString();
        String taskName = context.getJobDetail().getKey().getName();
        TaskMonitor monitor = new TaskMonitor(taskName, jobKey);
        monitorMap.put(jobKey, monitor);
        // 将监控实例同时存储到Map和ThreadLocal中，确保两种方式都能访问到同一个实例
        threadLocalMonitor.set(monitor);
        return true;
    }

    @Override
    public final boolean after(JobExecutionContext context) {
        String jobKey = context.getJobDetail().getKey().toString();
        TaskMonitor monitor = monitorMap.remove(jobKey);
        threadLocalMonitor.remove();
        taskResourceInfo = monitor.getTaskResourceInfo();
        return true;
    }

    @Override
    public final void integration(JobExecutionContext context) {
        // save resource info
        // 将监控数据保存到数据库中
        TaskMysqlDao.saveTaskResourceInfo(taskResourceInfo);
    }

    /**
     * 任务执行前调用
     *
     * @param context 任务执行上下文
     */
    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        String jobKey = context.getJobDetail().getKey().toString();

        // 通过Quartz监听器方式启动监控
        monitorMap.get(jobKey).start();

        logger.debug("开始监控任务资源: {}", jobKey);
    }

    /**
     * 任务执行被拒绝时调用（例如任务被暂停）
     *
     * @param context 任务执行上下文
     */
    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        String jobKey = context.getJobDetail().getKey().toString();

        // 移除监控实例
        monitorMap.remove(jobKey);
        threadLocalMonitor.remove();

        logger.debug("任务执行被拒绝: {}", jobKey);
    }

    /**
     * 任务执行完成后调用（无论成功还是失败）
     *
     * @param context      任务执行上下文
     * @param jobException 任务执行异常，如果没有异常则为null
     */
    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        String jobKey = context.getJobDetail().getKey().toString();

        TaskMonitor monitor = monitorMap.get(jobKey);

        if (monitor != null) {
            try {
                // 停止监控并输出统计信息
                monitor.stop();

                // 如果任务执行出现异常，记录异常信息
                if (jobException != null) {
                    logger.error("任务 {} 执行出现异常: ", jobKey, jobException);
                }
            } catch (Exception e) {
                logger.error("任务 {} 资源监控数据收集失败: ", jobKey, e);
            }
        }

        logger.debug("任务 {} 执行完成", jobKey);
    }


    /**
     * 任务资源信息数据类
     */
    public static class TaskResourceInfo {
        private final String jobKey;
        private final String taskName;
        private final long executionTimeMillis;
        private final long cpuTimeNanos;
        private final long memoryUsedBytes;
        private final long maxMemoryUsedBytes;
        private final List<Long> memorySamples;

        public TaskResourceInfo(String jobKey, String taskName, long executionTimeMillis, long cpuTimeNanos,
                                long memoryUsedBytes, long maxMemoryUsedBytes, List<Long> memorySamples) {
            this.jobKey = jobKey;
            this.taskName = taskName;
            this.executionTimeMillis = executionTimeMillis;
            this.cpuTimeNanos = cpuTimeNanos;
            this.memoryUsedBytes = memoryUsedBytes;
            this.maxMemoryUsedBytes = maxMemoryUsedBytes;
            this.memorySamples = memorySamples;
        }

        @Override
        public String toString() {
            return String.format("TaskResourceInfo{taskName='%s', executionTimeMillis=%d, cpuTimeNanos=%d, " +
                            "memoryUsedBytes=%d, maxMemoryUsedBytes=%d, memorySamplesSize=%d}",
                    taskName, executionTimeMillis, cpuTimeNanos, memoryUsedBytes, maxMemoryUsedBytes,
                    memorySamples != null ? memorySamples.size() : 0);
        }

        // Getters
        public String getTaskName() {
            return taskName;
        }

        public long getExecutionTimeMillis() {
            return executionTimeMillis;
        }

        public long getCpuTimeNanos() {
            return cpuTimeNanos;
        }

        public long getMemoryUsedBytes() {
            return memoryUsedBytes;
        }

        public long getMaxMemoryUsedBytes() {
            return maxMemoryUsedBytes;
        }

        public List<Long> getMemorySamples() {
            return memorySamples;
        }

        public String getJobKey() {
            return jobKey;
        }
    }

    /**
     * 任务监控内部类
     */
    private static class TaskMonitor {
        StopWatch stopWatch;
        private final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        private final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        private long startCpuTime;
        private long endCpuTime;
        private long cpuTimeUsed = 0;
        private long startMemoryUsed;
        private long maxMemoryUsed = 0;
        private volatile boolean monitoringMemory = false;
        private Thread memoryMonitorThread;

        // 存储内存采集数据
        private final List<Long> memorySamples = new ArrayList<>();

        // 用于存储任务名称
        private final String taskName;

        private final String jobKey;

        public TaskMonitor(String taskName, String jobKey) {
            this.taskName = taskName;
            this.stopWatch = new StopWatch("TaskResources-" + taskName);
            this.jobKey = jobKey;
        }

        public void start() {
            // 记录开始时的CPU时间
            if (threadMXBean.isCurrentThreadCpuTimeSupported()) {
                startCpuTime = threadMXBean.getCurrentThreadCpuTime();
            }

            // 记录开始时的内存使用情况
            startMemoryUsed = memoryMXBean.getHeapMemoryUsage().getUsed();

            // 清空之前的内存采集数据
            memorySamples.clear();

            // 启动内存监控线程来追踪内存使用峰值
            startMemoryMonitoring();

            stopWatch.start();
        }

        public void stop() {
            // 停止计时
            stopWatch.stop();

            // 停止内存监控
            stopMemoryMonitoring();

            // 记录CPU使用时间
            if (threadMXBean.isCurrentThreadCpuTimeSupported()) {
                endCpuTime = threadMXBean.getCurrentThreadCpuTime();
            }

            // 记录CPU使用时间
            cpuTimeUsed = endCpuTime - startCpuTime;

            // 输出统计信息
            logger.info("任务 {} 执行统计信息:", taskName);
            logger.info("  执行时间: {} ms", stopWatch.getLastTaskTimeMillis());
            logger.info("  CPU 使用时间: {} ns ({} ms)", cpuTimeUsed, cpuTimeUsed / 1_000_000);
            logger.info("  内存使用变化: {} bytes ({} KB)", (memoryMXBean.getHeapMemoryUsage().getUsed() - startMemoryUsed),
                    (memoryMXBean.getHeapMemoryUsage().getUsed() - startMemoryUsed) / 1024);
            logger.info("  内存使用峰值: {} bytes ({} KB)", maxMemoryUsed, maxMemoryUsed / 1024);

            // 输出内存采集数据
            if (!memorySamples.isEmpty()) {
                logger.info("  内存采集数据 (共 {} 次采样):", memorySamples.size());
                for (int i = 0; i < memorySamples.size(); i++) {
                    long memory = memorySamples.get(i);
                    logger.info("    采样 {}: {} bytes ({} KB)", i + 1, memory, memory / 1024);
                }
            }

            logger.info("  详细时间统计:\n{}", stopWatch.prettyPrint());
        }

        /**
         * 获取任务资源信息
         *
         * @return 任务资源信息对象
         */
        public TaskResourceInfo getTaskResourceInfo() {
            long executionTime = stopWatch != null ? stopWatch.getTotalTimeMillis() : 0;
            long memoryUsed = memoryMXBean.getHeapMemoryUsage().getUsed() - startMemoryUsed;
            return new TaskResourceInfo(jobKey, taskName, executionTime, cpuTimeUsed,
                    memoryUsed, maxMemoryUsed, new ArrayList<>(memorySamples));
        }

        /**
         * 启动内存监控线程
         */
        private void startMemoryMonitoring() {
            monitoringMemory = true;
            maxMemoryUsed = 0;

            memoryMonitorThread = new Thread(() -> {
                while (monitoringMemory) {
                    long currentMemoryUsed = memoryMXBean.getHeapMemoryUsage().getUsed();

                    // 记录每次采集到的内存值
                    memorySamples.add(currentMemoryUsed);

                    if (currentMemoryUsed > maxMemoryUsed) {
                        maxMemoryUsed = currentMemoryUsed;
                    }

                    // 短暂休眠以减少CPU使用
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });

            memoryMonitorThread.setDaemon(true);
            memoryMonitorThread.setName("MemoryMonitor-" + taskName);
            memoryMonitorThread.start();
        }

        /**
         * 停止内存监控
         */
        private void stopMemoryMonitoring() {
            monitoringMemory = false;
            if (memoryMonitorThread != null) {
                try {
                    memoryMonitorThread.join(100); // 等待最多100毫秒
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
package org.poying.base.cf;

import org.poying.base.ext.pyext.TaskResourcesSurround;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import javax.sql.DataSource;
import java.util.Properties;

public class QuartzConfig {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private TaskResourcesSurround taskResourcesSurround;
    
    @Autowired
    private TaskScheduleProperties taskScheduleProperties;

    /**
     * 配置JobFactory，使Quartz能够使用Spring的依赖注入
     */
    public static class AutowiringSpringBeanJobFactory extends SpringBeanJobFactory {
        private final AutowireCapableBeanFactory beanFactory;

        public AutowiringSpringBeanJobFactory(AutowireCapableBeanFactory beanFactory) {
            this.beanFactory = beanFactory;
        }

        @Override
        protected Object createJobInstance(@NonNull TriggerFiredBundle bundle) throws Exception {
            Object jobInstance = super.createJobInstance(bundle);
            beanFactory.autowireBean(jobInstance);
            return jobInstance;
        }
    }

    /**
     * 配置SchedulerFactoryBean
     *
     * @param dataSource 数据源
     * @return SchedulerFactoryBean
     */
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(DataSource dataSource) {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory(
                applicationContext.getAutowireCapableBeanFactory());
        factory.setJobFactory(jobFactory);
        factory.setDataSource(dataSource);

        // 设置Quartz属性
        Properties properties = new Properties();
        
        // Scheduler configuration
        TaskScheduleProperties.Scheduler schedulerConfig = taskScheduleProperties.getScheduler();
        properties.put("org.quartz.scheduler.instanceName", schedulerConfig.getInstanceName());
        properties.put("org.quartz.scheduler.instanceId", schedulerConfig.getInstanceId());

        // 线程池配置
        TaskScheduleProperties.ThreadPool threadPoolConfig = taskScheduleProperties.getThreadPool();
        properties.put("org.quartz.threadPool.class", threadPoolConfig.getClazz());
        properties.put("org.quartz.threadPool.threadCount", String.valueOf(threadPoolConfig.getThreadCount()));
        properties.put("org.quartz.threadPool.threadPriority", String.valueOf(threadPoolConfig.getThreadPriority()));

        // JobStore配置
        TaskScheduleProperties.JobStore jobStoreConfig = taskScheduleProperties.getJobStore();
        properties.put("org.quartz.jobStore.class", jobStoreConfig.getClazz());
        properties.put("org.quartz.jobStore.driverDelegateClass", jobStoreConfig.getDriverDelegateClass());
        properties.put("org.quartz.jobStore.tablePrefix", jobStoreConfig.getTablePrefix());
        properties.put("org.quartz.jobStore.isClustered", String.valueOf(jobStoreConfig.isClustered()));
        properties.put("org.quartz.jobStore.useProperties", String.valueOf(jobStoreConfig.isUseProperties()));
        properties.put("org.quartz.jobStore.dataSource", jobStoreConfig.getDataSource());

        // 数据源配置
        TaskScheduleProperties.DataSource.MyDS dataSourceConfig = taskScheduleProperties.getDataSource().getMyDS();
        properties.put("org.quartz.dataSource.myDS.driver", dataSourceConfig.getDriver());
        properties.put("org.quartz.dataSource.myDS.URL", dataSourceConfig.getUrl());
        properties.put("org.quartz.dataSource.myDS.user", dataSourceConfig.getUser());
        properties.put("org.quartz.dataSource.myDS.password", dataSourceConfig.getPassword());
        properties.put("org.quartz.dataSource.myDS.maxConnections", String.valueOf(dataSourceConfig.getMaxConnections()));
        properties.put("org.quartz.dataSource.myDS.validationQuery", dataSourceConfig.getValidationQuery());

        factory.setQuartzProperties(properties);
        factory.setApplicationContext(applicationContext);

        // 注册任务监控监听器
        factory.setGlobalJobListeners(taskResourcesSurround);

        return factory;
    }
}
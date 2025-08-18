package org.poying.base.spring;

import org.poying.base.cf.QuartzConfig;
import org.poying.base.cf.TaskScheduleProperties;
import org.poying.base.ext.pyext.TaskResourcesSurround;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

import java.sql.Driver;

/**
 * Task Schedule自动配置类
 *
 * @author poying
 */
@Configuration
@ConditionalOnClass({Driver.class})
@EnableConfigurationProperties({TaskScheduleProperties.class, DataSourceProperties.class})
@Import(QuartzConfig.class)
public class TaskScheduleAutoConfiguration {
    
    @Bean
    @ConditionalOnMissingBean
    public DataSource dataSource(DataSourceProperties dataSourceProperties) {
        // 使用Spring Boot默认的数据源配置创建DataSource
        return dataSourceProperties.initializeDataSourceBuilder().build();
    }
    
    @Bean
    @ConditionalOnMissingBean
    public DataSourceTransactionManager dataSourceTransactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
    
    @Bean
    @ConditionalOnMissingBean
    public TaskResourcesSurround taskResourcesSurround() {
        return new TaskResourcesSurround();
    }
}
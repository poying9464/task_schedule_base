package org.poying.base.cf;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConfigurationProperties(prefix = "poying.task")
public class TaskScheduleProperties {
    
    // Scheduler configuration
    private Scheduler scheduler = new Scheduler();
    
    // Thread pool configuration
    private ThreadPool threadPool = new ThreadPool();
    
    // Job store configuration
    private JobStore jobStore = new JobStore();
    
    // Data source configuration
    private DataSource dataSource = new DataSource();
    
    // Getters and setters for top-level configuration objects
    public Scheduler getScheduler() {
        return scheduler;
    }
    
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }
    
    public ThreadPool getThreadPool() {
        return threadPool;
    }
    
    public void setThreadPool(ThreadPool threadPool) {
        this.threadPool = threadPool;
    }
    
    public JobStore getJobStore() {
        return jobStore;
    }
    
    public void setJobStore(JobStore jobStore) {
        this.jobStore = jobStore;
    }
    
    public DataSource getDataSource() {
        return dataSource;
    }
    
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // Inner classes representing configuration sections
    
    public static class Scheduler {

        private String instanceName = "MyQuartzScheduler";

        private String instanceId = "AUTO";
        
        // Getters and setters
        public String getInstanceName() {
            return instanceName;
        }
        
        public void setInstanceName(String instanceName) {
            this.instanceName = instanceName;
        }
        
        public String getInstanceId() {
            return instanceId;
        }
        
        public void setInstanceId(String instanceId) {
            this.instanceId = instanceId;
        }
    }
    
    public static class ThreadPool {
        private String clazz = "org.quartz.simpl.SimpleThreadPool";
        private int threadCount = 5;
        private int threadPriority = 5;
        
        // Getters and setters
        public String getClazz() {
            return clazz;
        }
        
        public void setClazz(String clazz) {
            this.clazz = clazz;
        }
        
        public int getThreadCount() {
            return threadCount;
        }
        
        public void setThreadCount(int threadCount) {
            this.threadCount = threadCount;
        }
        
        public int getThreadPriority() {
            return threadPriority;
        }
        
        public void setThreadPriority(int threadPriority) {
            this.threadPriority = threadPriority;
        }
    }
    
    public static class JobStore {
        private String clazz = "org.quartz.impl.jdbcjobstore.JobStoreTX";
        private String driverDelegateClass = "org.quartz.impl.jdbcjobstore.StdJDBCDelegate";
        private String tablePrefix = "QRTZ_";
        private boolean clustered = false;
        private boolean useProperties = false;
        private String dataSource = "myDS";
        
        // Getters and setters
        public String getClazz() {
            return clazz;
        }
        
        public void setClazz(String clazz) {
            this.clazz = clazz;
        }
        
        public String getDriverDelegateClass() {
            return driverDelegateClass;
        }
        
        public void setDriverDelegateClass(String driverDelegateClass) {
            this.driverDelegateClass = driverDelegateClass;
        }
        
        public String getTablePrefix() {
            return tablePrefix;
        }
        
        public void setTablePrefix(String tablePrefix) {
            this.tablePrefix = tablePrefix;
        }
        
        public boolean isClustered() {
            return clustered;
        }
        
        public void setClustered(boolean clustered) {
            this.clustered = clustered;
        }
        
        public boolean isUseProperties() {
            return useProperties;
        }
        
        public void setUseProperties(boolean useProperties) {
            this.useProperties = useProperties;
        }
        
        public String getDataSource() {
            return dataSource;
        }
        
        public void setDataSource(String dataSource) {
            this.dataSource = dataSource;
        }
    }
    
    public static class DataSource {
        private MyDS myDS = new MyDS();
        
        // Getters and setters
        public MyDS getMyDS() {
            return myDS;
        }
        
        public void setMyDS(MyDS myDS) {
            this.myDS = myDS;
        }
        
        public static class MyDS {
            private String driver = "com.mysql.cj.jdbc.Driver";
            private String url = "";
            private String user = "";
            private String password = "";
            private int maxConnections = 10;
            private String validationQuery = "select 1";
            
            // Getters and setters
            public String getDriver() {
                return driver;
            }
            
            public void setDriver(String driver) {
                this.driver = driver;
            }

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }

            public String getUser() {
                return user;
            }
            
            public void setUser(String user) {
                this.user = user;
            }
            
            public String getPassword() {
                return password;
            }
            
            public void setPassword(String password) {
                this.password = password;
            }
            
            public int getMaxConnections() {
                return maxConnections;
            }

            public void setMaxConnections(int maxConnections) {
                this.maxConnections = maxConnections;
            }
            
            public String getValidationQuery() {
                return validationQuery;
            }
            
            public void setValidationQuery(String validationQuery) {
                this.validationQuery = validationQuery;
            }
        }
    }
}
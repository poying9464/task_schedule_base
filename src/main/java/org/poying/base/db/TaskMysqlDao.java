package org.poying.base.db;

import org.poying.base.ext.pyext.TaskResourcesSurround;
import org.poying.base.utils.GetBeanUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TaskMysqlDao {

    private static final DataSource dataSource;

    static {
        dataSource = GetBeanUtils.getBean(DataSource.class);
    }

    public static void saveTaskResourceInfo(TaskResourcesSurround.TaskResourceInfo taskResourceInfo) {
        // 使用JDBC保存任务资源信息
        try (Connection connection = dataSource.getConnection()) {
            String sql = "INSERT INTO task_resource_info (jobKey, task_name, execution_time, cpu_time, memory_used, max_memory_used, memory_samples, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (java.sql.PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                // 开启事务
                connection.setAutoCommit(false);
                preparedStatement.setString(1, taskResourceInfo.getJobKey());
                preparedStatement.setString(2, taskResourceInfo.getTaskName());
                preparedStatement.setLong(3, taskResourceInfo.getExecutionTimeMillis());
                preparedStatement.setLong(4, taskResourceInfo.getCpuTimeNanos());
                preparedStatement.setLong(5, taskResourceInfo.getMemoryUsedBytes());
                preparedStatement.setLong(6, taskResourceInfo.getMaxMemoryUsedBytes());
                preparedStatement.setString(7, taskResourceInfo.getMemorySamples().toString());
                preparedStatement.setTimestamp(8, new java.sql.Timestamp(System.currentTimeMillis()));
                preparedStatement.setTimestamp(9, new java.sql.Timestamp(System.currentTimeMillis()));
                preparedStatement.executeUpdate();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new RuntimeException(e);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isSuccessful(String jobKey, String taskName) {
        return true;
    }
}

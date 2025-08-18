package org.poying.base.db;

import org.poying.base.ext.pyext.TaskResourcesSurround;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TaskResourceInfoDao {
    public static void save(TaskResourcesSurround.TaskResourceInfo taskResourceInfo, DataSource dataSource) {
        // 使用JDBC保存任务资源信息
        try (Connection connection = dataSource.getConnection()) {
            String sql = "INSERT INTO task_resource_info (task_name, execution_time, cpu_time, memory_used, max_memory_used, memory_samples) VALUES (?, ?, ?, ?, ?, ?)";
            try (java.sql.PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                // 开启事务
                connection.setAutoCommit(false);
                preparedStatement.setString(1, taskResourceInfo.getTaskName());
                preparedStatement.setLong(2, taskResourceInfo.getExecutionTimeMillis());
                preparedStatement.setLong(3, taskResourceInfo.getCpuTimeNanos());
                preparedStatement.setLong(4, taskResourceInfo.getMemoryUsedBytes());
                preparedStatement.setLong(5, taskResourceInfo.getMaxMemoryUsedBytes());
                preparedStatement.setString(6, taskResourceInfo.getMemorySamples().toString());
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
}

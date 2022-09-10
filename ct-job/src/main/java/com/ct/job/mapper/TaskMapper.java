package com.ct.job.mapper;

import com.ct.job.model.Task;
import com.ct.job.model.TaskDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TaskMapper {
    void insertTask(@Param("task") Task task);
    List<String> getAllTask();
    int updateTaskStatu(@Param("taskId") long taskId, @Param("status") int status, @Param("nodeId") long nodeId);
    List<Task> getNotStartTask(@Param("time") int time);
    int updateWithVersion(@Param("task")Task task);
    void updateTask(@Param("task")Task task);
    Task get(@Param("id")Long id);
    int updateTaskDetail(@Param("taskDetail") TaskDetail taskDetail);
    long insertTaskDetail(@Param("taskDetail")TaskDetail taskDetail);
    List<Task> listRecoverTasks(@Param("timeout") int timeout);
    void deleteTask();
    void deleteTaskDetail();
    TaskDetail getDetail(@Param("taskId") Long taskId);
}

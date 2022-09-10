package com.ct.job.register;

import com.ct.job.enums.NotifyCmd;
import com.ct.job.model.Node;
import com.ct.job.model.Task;
import com.ct.job.model.TaskDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface Register {
    void insertTask(Task task);
    List<String> getAllTask();
    int updateTaskStatu(long taskId, int status, long nodeId);
    List<Task> getNotStartTask(int time);
    int updateWithVersion(Task task);
    void updateTask(Task task);
    Task get(Long id);
    int updateTaskDetail(TaskDetail taskDetail);
    long insertTaskDetail(TaskDetail taskDetail);
    List<Task> listRecoverTasks( int timeout);
    void deleteTask();
    void deleteTaskDetail();
    TaskDetail getDetail( Long taskId);
    List<Node> getEnableNodes( int timeout);
    Node getByNodeId(Long nodeId);
    long getNextRowNum();
    long insert( Node node);
    int updateHeartBeat(Long nodeId);
    int resetNotifyInfo( Long nodeId,NotifyCmd cmd);
    int addCounts(Long nodeId);
    int count();
}

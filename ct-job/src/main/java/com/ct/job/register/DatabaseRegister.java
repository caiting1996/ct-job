package com.ct.job.register;

import com.ct.job.mapper.TaskMapper;
import com.ct.job.enums.NotifyCmd;
import com.ct.job.mapper.NodeMapper;
import com.ct.job.model.Node;
import com.ct.job.model.Task;
import com.ct.job.model.TaskDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
@Lazy
@Component("databaseRegister")
public class DatabaseRegister implements Register {
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private NodeMapper nodeMapper;
    @Override
    public void insertTask(Task task) {
        taskMapper.insertTask(task);
    }

    @Override
    public List<String> getAllTask() {
        return taskMapper.getAllTask();
    }

    @Override
    public int updateTaskStatu(long taskId, int status, long nodeId) {
        return taskMapper.updateTaskStatu(taskId,status,nodeId);
    }

    @Override
    public List<Task> getNotStartTask(int time) {
        return taskMapper.getNotStartTask(time);
    }

    @Override
    public int updateWithVersion(Task task) {
        return taskMapper.updateWithVersion(task);
    }

    @Override
    public void updateTask(Task task) {
        taskMapper.updateTask(task);
    }

    @Override
    public Task get(Long id) {
        return taskMapper.get(id);
    }

    @Override
    public int updateTaskDetail(TaskDetail taskDetail) {
        return taskMapper.updateTaskDetail(taskDetail);
    }

    @Override
    public long insertTaskDetail(TaskDetail taskDetail) {
        return taskMapper.insertTaskDetail(taskDetail);
    }

    @Override
    public List<Task> listRecoverTasks(int timeout) {
        return taskMapper.listRecoverTasks(timeout);
    }

    @Override
    public void deleteTask() {
        taskMapper.deleteTask();
    }

    @Override
    public void deleteTaskDetail() {
        taskMapper.deleteTaskDetail();
    }

    @Override
    public TaskDetail getDetail(Long taskId) {
        return taskMapper.getDetail(taskId);
    }

    @Override
    public List<Node> getEnableNodes(int timeout) {
        return nodeMapper.getEnableNodes(timeout);
    }

    @Override
    public Node getByNodeId(Long nodeId) {
        return nodeMapper.getByNodeId(nodeId);
    }

    @Override
    public long getNextRowNum() {
        return nodeMapper.getNextRowNum();
    }

    @Override
    public long insert(Node node) {
        return nodeMapper.insert(node);
    }

    @Override
    public int updateHeartBeat(Long nodeId) {
        return nodeMapper.updateHeartBeat(nodeId);
    }

    @Override
    public int resetNotifyInfo(Long nodeId, NotifyCmd cmd) {
        return nodeMapper.resetNotifyInfo(nodeId,cmd);
    }

    @Override
    public int addCounts(Long nodeId) {
        return nodeMapper.addCounts(nodeId);
    }

    @Override
    public int count() {
        return nodeMapper.count();
    }
}

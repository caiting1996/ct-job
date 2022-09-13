package com.ct.job.state;

import com.ct.job.enums.TaskStatus;
import com.ct.job.model.Task;
import com.ct.job.model.TaskDetail;

import java.text.ParseException;
import java.util.Date;

/**
 * 任务状态抽象类
 */
public class AbstractTaskState implements TaskState {
    protected Task task;
    public AbstractTaskState(Task task){
        this.task=task;
    }
    @Override
    public Integer notStart() throws ParseException {
        TaskDetail taskDetail=task.getTaskDetail();
        taskDetail.setStatus(TaskStatus.NOT_STARTED.getId());
        task.setTaskDetail(taskDetail);
        task.setStatus(TaskStatus.NOT_STARTED.getId());
        task.setNowState(task.getNoStartState());
        return null;
    }

    @Override
    public Integer pending() {
        TaskDetail taskDetail=task.getTaskDetail();
        taskDetail.setStatus(TaskStatus.PENDING.getId());
        taskDetail.setNodeId(task.getNodeId());
        task.setTaskDetail(taskDetail);
        task.setStatus(TaskStatus.PENDING.getId());
        task.setNowState(task.getPendingState());
        return null;
    }

    @Override
    public boolean doing() {
        TaskDetail taskDetail=task.getTaskDetail();
        taskDetail.setStatus(TaskStatus.DOING.getId());
        taskDetail.setStartTime(new Date());
        task.setTaskDetail(taskDetail);
        task.setStatus(TaskStatus.DOING.getId());
        task.setNowState(task.getDoingState());
        return false;
    }

    @Override
    public boolean error() {
        TaskDetail taskDetail=task.getTaskDetail();
        taskDetail.setStatus(TaskStatus.ERROR.getId());
        task.setTaskDetail(taskDetail);
        task.setStatus(TaskStatus.ERROR.getId());
        task.setNowState(task.getErrorState());
        return false;
    }

    @Override
    public Integer finish() throws ParseException {
        TaskDetail taskDetail=task.getTaskDetail();
        taskDetail.setStatus(TaskStatus.FINISH.getId());
        taskDetail.setEndTime(new Date());
        task.setTaskDetail(taskDetail);
        task.setStatus(TaskStatus.FINISH.getId());
        task.setNowState(task.getFinishState());
        return null;
    }

    @Override
    public boolean stop() throws ParseException {
        TaskDetail taskDetail=task.getTaskDetail();
        taskDetail.setStatus(TaskStatus.STOP.getId());
        taskDetail.setEndTime(new Date());
        task.setTaskDetail(taskDetail);
        task.setStatus(TaskStatus.STOP.getId());
        task.setNowState(task.getStopState());
        return false;
    }
}

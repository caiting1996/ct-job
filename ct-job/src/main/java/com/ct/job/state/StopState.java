package com.ct.job.state;

import com.ct.job.model.Task;

/**
 * 任务停止状态
 */
public class StopState extends AbstractTaskState{
    private Task task;
    public StopState(Task task){
        super(task);
    }
}

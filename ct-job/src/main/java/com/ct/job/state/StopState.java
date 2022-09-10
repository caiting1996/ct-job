package com.ct.job.state;

import com.ct.job.model.Task;


public class StopState extends AbstractTaskState{
    private Task task;
    public StopState(Task task){
        super(task);
    }
}

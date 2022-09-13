package com.ct.job.state;

import com.ct.job.model.Task;

/**
 * 任务完成状态
 */
public class FinishState extends AbstractTaskState{
    public FinishState(Task task){
        super(task);
    }
}

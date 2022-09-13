package com.ct.job.state;

import com.ct.job.model.Task;

/**
 * 任务异常状态
 */
public class ErrorState extends AbstractTaskState {

    public ErrorState(Task task){
        super(task);
    }

}

package com.ct.job.state;

import com.ct.job.model.Task;

import java.text.ParseException;

/**
 * 任务等待状态
 */
public class PendingState extends AbstractTaskState{
    public PendingState(Task task){
        super(task);
    }
    @Override
    public Integer notStart() throws ParseException {
        super.notStart();
        task.getRegister().updateTask(task);
        return null;
    }

    @Override
    public boolean doing() {
        super.doing();
        task.getRegister().updateTask(task);
        return true;
    }
}

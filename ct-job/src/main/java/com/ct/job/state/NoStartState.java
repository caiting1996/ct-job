package com.ct.job.state;

import com.ct.job.model.Task;

import java.text.ParseException;

/**
 * 任务未开始状态
 */
public class NoStartState extends AbstractTaskState{
    public NoStartState(Task task){
        super(task);
    }
    @Override
    public Integer notStart() throws ParseException {
        super.notStart();
        task.setNextStartTime();
        int n = task.getRegister().updateWithVersion(task);
        return n;
    }
    @Override
    public Integer pending() {
        super.pending();
        task.getRegister().updateTaskDetail(task.getTaskDetail());
        Integer n=task.getRegister().updateWithVersion(task);
        return n;
    }
}

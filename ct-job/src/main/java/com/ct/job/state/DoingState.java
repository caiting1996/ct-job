package com.ct.job.state;

import com.ct.job.model.Task;

import java.text.ParseException;


public class DoingState extends AbstractTaskState {

    public DoingState(Task task){
        super(task);
    }
    @Override
    public Integer notStart() throws ParseException {
        super.notStart();
        task.setSuccessCount(task.getSuccessCount() + 1);
        int n = task.getRegister().updateWithVersion(task);
        return n;
    }

    @Override
    public Integer finish() throws ParseException {
        super.finish();
        task.setSuccessCount(task.getSuccessCount() + 1);
        int n = task.getRegister().updateWithVersion(task);
        return n;
    }

    @Override
    public boolean stop() throws ParseException {
        super.stop();
        task.getRegister().updateTask(task);
        return true;
    }
}

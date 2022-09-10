package com.ct.job.handles;

import com.ct.job.schedule.TaskExcutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.text.ParseException;


@Lazy
@Component("STOP_TASK")
public class StopTaskHandler implements NotifyHandler<Long> {

    @Autowired
    private TaskExcutor taskExcutor;

    @Override
    public void update(Long taskId) throws ParseException {
        taskExcutor.stop(taskId);
    }

}

package com.ct.job.state;

import java.text.ParseException;

/**
 * 任务状态接口
 */
public interface TaskState {

    Integer notStart() throws ParseException;
    Integer pending();
    boolean doing();
    boolean error();
    Integer finish() throws ParseException;
    boolean stop() throws ParseException;
}

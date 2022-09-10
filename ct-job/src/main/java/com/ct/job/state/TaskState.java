package com.ct.job.state;

import java.text.ParseException;

public interface TaskState {

    Integer notStart() throws ParseException;
    Integer pending();
    boolean doing();
    boolean error();
    Integer finish() throws ParseException;
    boolean stop() throws ParseException;
}

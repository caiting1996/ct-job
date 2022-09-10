package com.ct.job.handles;

import com.ct.job.utils.SpringContextUtil;
import com.ct.job.enums.NotifyCmd;

import java.text.ParseException;


public interface NotifyHandler<T> {

    static NotifyHandler chooseHandler(NotifyCmd notifyCmd) {
        return SpringContextUtil.getByTypeAndName(NotifyHandler.class,notifyCmd.toString());
    }

    public void update(T t) throws ParseException;

}

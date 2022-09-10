package com.ct.job.register;

import com.ct.job.config.JobConfig;
import com.ct.job.utils.SpringContextUtil;

public class RegisterContext {
    private static Register register;
    public static Register chooseRegister(){
        JobConfig config=(JobConfig) SpringContextUtil.getBean(JobConfig.class);
        String type=config.getRegisterType();
        if(type.equals("database")){
            register= (Register) SpringContextUtil.getBean("databaseRegister");
        }else if(type.equals("zookeeper")){
            register= (Register) SpringContextUtil.getBean("zookeeperRegister");
        }
        return register;
    }

}

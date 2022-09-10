package com.ct.job.utils;

import com.ct.job.config.JobConfig;
import com.ct.job.mapper.NodeMapper;

public class JobHelper {
    private static NodeMapper nodeMapper= (NodeMapper) SpringContextUtil.getBean(NodeMapper.class);
    private static JobConfig config= (JobConfig) SpringContextUtil.getBean(JobConfig.class);
    public static Long getNodeId(){return config.getNodeId(); }
    public static int getNodeNum(){return nodeMapper.count();}
}

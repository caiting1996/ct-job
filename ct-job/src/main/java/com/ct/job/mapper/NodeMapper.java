package com.ct.job.mapper;

import com.ct.job.enums.NotifyCmd;
import com.ct.job.model.Node;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface NodeMapper {
    List<Node> getEnableNodes(@Param("timeout") int timeout);
    Node getByNodeId(@Param("nodeId")Long nodeId);
    long getNextRowNum();
    long insert(@Param("node") Node node);
    int updateHeartBeat(Long nodeId);
    int resetNotifyInfo(@Param("nodeId") Long nodeId,@Param("cmd") NotifyCmd cmd);
    int addCounts(@Param("nodeId") Long nodeId);
    int count();
}

package com.ct.job.strategy;

import com.ct.job.model.Node;
import com.ct.job.model.Task;

import java.util.List;

/**
 * 默认的来者不惧的策略，只要能抢到就要，其实这种方式等同于随机分配任务，因为谁可以抢到不一定
 */
public class DefaultStrategy implements Strategy {

	@Override
	public boolean accept(List<Node> nodes, Task task, Long myNodeId) {
		return true;
	}

}

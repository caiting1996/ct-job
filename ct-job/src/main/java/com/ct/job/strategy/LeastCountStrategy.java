package com.ct.job.strategy;

import com.ct.job.model.Node;
import com.ct.job.model.Task;

import java.util.List;
import java.util.Optional;

/**
 * 最少处理任务次数策略，也就是每次任务来了，看看自己是不是处理任务次数最少的，是就可以消费这个任务
 */
public class LeastCountStrategy implements Strategy {

	@Override
	public boolean accept(List<Node> nodes, Task task, Long myNodeId) {

		/**
		 * 获取次数最少的那个节点,这里可以类比成先按counts升序排列然后取第一个元素
		 * 然后是自己就返回true
		 */
		Optional<Node> min = nodes.stream().min((o1, o2) -> o1.getCounts().compareTo(o2.getCounts()));
		
		return min.isPresent()? min.get().getNodeId() == myNodeId : false;
	}
	
}

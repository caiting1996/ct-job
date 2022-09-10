package com.ct.job.strategy;

import com.ct.job.model.Node;
import com.ct.job.model.Task;

import java.util.List;

/**
 * 按照任务ID hash方式针对有效节点个数取余，然后余数+1后和各个节点的顺序号匹配，
 * 这种方式效果其实等同于轮询，因为任务id是自增的
 */
public class IdHashStrategy implements Strategy {

	/**
	 * 这里的nodes集合必然不会为空，外面调度那判断了，而且是按照nodeId的升序排列的
	 */
	@Override
	public boolean accept(List<Node> nodes, Task task, Long myNodeId) {
		int size = nodes.size();
		long taskId = task.getId();
		/**
		 * 找到自己的节点
		 */
		Node myNode = nodes.stream().filter(node -> node.getNodeId() == myNodeId).findFirst().get();
		return myNode == null ? false : (taskId % size) + 1 == myNode.getRowNum();
	}

}

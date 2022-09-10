package com.ct.job.strategy;

import com.ct.job.model.Node;
import com.ct.job.model.Task;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 按权重的分配策略,方案如下，假如
 * 节点序号   1     ,2     ,3       ,4
 * 节点权重   2     ,3     ,3       ,2
 * 则取余后 0,1 | 2,3,4 | 5,6,7 | 8,9
 * 序号1可以消费按照权重的和取余后小于2的
 * 序号2可以消费按照权重的和取余后大于等于2小于2+3的
 * 序号3可以消费按照权重的和取余后大于等于2+3小于2+3+3的
 * 序号3可以消费按照权重的和取余后大于等于2+3+3小于2+3+3+2的
 * 总结：本节点可以消费的按照权重的和取余后大于等于前面节点的权重和小于包括自己的权重和的这个范围
 * 不知道有没有大神有更好的算法思路
 */
public class WeightStrategy implements Strategy {

	@Override
	public boolean accept(List<Node> nodes, Task task, Long myNodeId) {
		Node myNode = nodes.stream().filter(node -> node.getNodeId() == myNodeId).findFirst().get();
		if(myNode == null) {
			return false;
		}
		/**
		 * 计算本节点序号前面的节点的权重和
		 */
		int preWeightSum = nodes.stream().filter(node -> node.getRowNum() < myNode.getRowNum()).collect(Collectors.summingInt(Node::getWeight));
		/**
		 * 计算全部权重的和
		 */
		int weightSum = nodes.stream().collect(Collectors.summingInt(Node::getWeight));
		/**
		 * 计算对权重和取余的余数
		 */
		int remainder = (int)(task.getId() % weightSum);
		return remainder >= preWeightSum && remainder < preWeightSum + myNode.getWeight();
	}
	
}

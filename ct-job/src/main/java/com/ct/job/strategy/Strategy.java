package com.ct.job.strategy;

import com.ct.job.model.Node;
import com.ct.job.model.Task;

import java.util.List;

/**
 * 抽象的策略接口
 */
public interface Strategy {

	/**
	 * 默认策略
	 */
	String DEFAULT = "default";
	
	/**
	 * 按任务ID hash取余再和自己节点序号匹配
	 */
	String ID_HASH = "id_hash";
	
	/**
	 * 最少执行次数
	 */
	String LEAST_COUNT = "least_count";
	
	/**
	 * 按节点权重
	 */
	String WEIGHT = "weight";
	
	
	public static Strategy choose(String key) {
		switch(key) {
			case ID_HASH:
				return new IdHashStrategy();
			case LEAST_COUNT:
				return new LeastCountStrategy();
			case WEIGHT:
				return new WeightStrategy();
			default:
				return new DefaultStrategy();
		}
	}
	
	public boolean accept(List<Node> nodes, Task task, Long myNodeId);
	
}

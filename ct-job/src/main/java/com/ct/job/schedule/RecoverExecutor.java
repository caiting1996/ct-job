package com.ct.job.schedule;

import com.ct.job.config.JobConfig;
import com.ct.job.handles.NotifyHandler;
import com.ct.job.model.Node;
import com.ct.job.enums.NotifyCmd;
import com.ct.job.model.Task;
import com.ct.job.register.RegisterContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.text.ParseException;
import java.util.List;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 任务恢复
 */
@Lazy
@Component
public class RecoverExecutor {

    private static final Logger logger = LoggerFactory.getLogger(RecoverExecutor.class);

    @Autowired
    private JobConfig config;

    /**
     * 创建节点心跳延时队列
      */
    private DelayQueue<DelayItem<Node>> heartBeatQueue = new DelayQueue<>();

    /**
     * 可以明确知道最多只会运行2个线程，直接使用系统自带工具
     */
    private ExecutorService bossPool = Executors.newFixedThreadPool(2);
    
    @PostConstruct
    public void init() {
    	/**
    	 * 如果恢复线程开关是开着，并且心跳开关也是开着
    	 */
    	if(config.isRecoverEnable() && config.isHeartBeatEnable()) {
    		/**
             * 初始化一个节点到心跳队列，延时为0，用来注册节点
             */
            heartBeatQueue.offer(new DelayItem<>(0,new Node(config.getNodeId())));
            /**
             * 执行心跳线程
             */
            bossPool.execute(new HeartBeat());
            /**
             * 执行异常恢复线程
             */
            bossPool.execute(new Recover());


    	}
    }

    class Recover implements Runnable {
        @Override
        public void run() {
            for (;;) {
                try {
                    /**
                     * 太累了，先睡会
                     */
                    Thread.sleep(config.getRecoverSeconds() * 1000);
                    /**
                     * 查找需要恢复的任务,这里界定需要恢复的任务是任务还没完成，并且所属执行节点超过3个
                     * 心跳周期没有更新心跳时间。由于这些任务由于当时执行节点没有来得及执行完就挂了，所以
                     * 只需要把状态再改回待执行，并且下次执行时间改成当前时间，让任务再次被调度一次
                     */
                    List<Task> tasks = RegisterContext.chooseRegister().listRecoverTasks(config.getHeartBeatSeconds() * 3);

                    if(tasks == null || tasks.isEmpty()) {
                    	return;
                    }
                   /**
                    * 先获取可用的节点列表
                    */
                    List<Node> nodes = RegisterContext.chooseRegister().getEnableNodes(config.getHeartBeatSeconds() * 2);
                   if(nodes == null || nodes.isEmpty()) {
                       return;
                   }
                   long maxNodeId = nodes.get(nodes.size() - 1).getNodeId();
                    for (Task task : tasks) {
                        /**
                         * 每个节点有一个恢复线程，为了避免不必要的竞争,从可用节点找到一个最靠近任务所属节点的节点
                         */
                        long currNodeId = chooseNodeId(nodes,maxNodeId,task.getNodeId());
                        long myNodeId = config.getNodeId();
                        /**
                         * 如果不该当前节点处理直接跳过
                         */
                        if(currNodeId != myNodeId) {
                            continue;
                        }
                        /**
                         * 直接将任务状态改成待执行，并且节点改成当前节点，有人可能会怀疑这里的安全性，可能会随着该事务的原主人节点的下一个
                         * 正好在这时候挂了，chooseNodeId得到的下一个节点就变了，其它节点获取到的下一个节点就变了，也会进入这里。不过就算这里
                         * 产生了竞争。如果我只是给事务换个主人。真正补偿由补偿线程完成，那边使用乐观锁去抢占事务，就会变得很安全。
                         */
                        task.setNodeId(config.getNodeId());
                    	resetTask(task);
                    }

                } catch (Exception e) {
                    logger.error("Get next task failed,cause by:{}", e);
                }
            }
        }

    }

    class HeartBeat implements Runnable {
        @Override
        public void run() {
            for(;;) {
                try {
                    /**
                     * 时间到了就可以从延时队列拿出节点对象，然后更新时间和序号，
                     * 最后再新建一个超时时间为心跳时间的节点对象放入延时队列，形成循环的心跳
                     */
                    DelayItem<Node> item = heartBeatQueue.take();
                    if(item != null && item.getItem() != null) {
                        Node node = item.getItem();
                        handHeartBeat(node);
                    }
                    heartBeatQueue.offer(new DelayItem<>(config.getHeartBeatSeconds() * 1000,new Node(config.getNodeId())));
                } catch (Exception e) {
                    logger.error("task heart beat error,cause by:{} ",e);
                }
            }
        }
    }

    /**
     * 处理节点心跳
     * @param node
     */
    private void handHeartBeat(Node node) throws ParseException {
        if(node == null) {
            return;
        }
        /**
         * 先看看数据库是否存在这个节点
         * 如果不存在：先查找下一个序号，然后设置到node对象中，最后插入
         * 如果存在：直接根据nodeId更新当前节点的序号和时间
         */
        Node currNode= RegisterContext.chooseRegister().getByNodeId(node.getNodeId());
        if(currNode == null) {
            node.setRowNum(RegisterContext.chooseRegister().getNextRowNum());
            RegisterContext.chooseRegister().insert(node);
        } else  {
            RegisterContext.chooseRegister().updateHeartBeat(node.getNodeId());
            NotifyCmd cmd = currNode.getNotifyCmd();
            String notifyValue = currNode.getNotifyValue();
            if(cmd != null && cmd != NotifyCmd.NO_NOTIFY) {
                /**
                 * 借助心跳做一下通知的事情，比如及时停止正在执行的任务
                 * 根据指令名称查找Handler
                 */
                NotifyHandler handler = NotifyHandler.chooseHandler(currNode.getNotifyCmd());
                if(handler == null || StringUtils.isEmpty(notifyValue)) {
                    return;
                }
                /**
                 * 先重置通知再说，以免每次心跳无限执行通知下面更新逻辑
                 */
                RegisterContext.chooseRegister().resetNotifyInfo(currNode.getNodeId(),cmd);
                /**
                 * 执行操作
                 */
                handler.update(Long.valueOf(notifyValue));
            }
            
        }


    }

    /**
     * 选择下一个节点
     * @param nodes
     * @param maxNodeId
     * @param nodeId
     * @return
     */
    private long chooseNodeId(List<Node> nodes,long maxNodeId,long nodeId) {
        if(nodes.size() == 0 || nodeId >= maxNodeId) {
            return nodes.get(0).getNodeId();
        }
        return nodes.stream().filter(node -> node.getNodeId() > nodeId).findFirst().get().getNodeId();
    }

    private void resetTask(Task task) throws ParseException {
        task.notStart();
    }

}

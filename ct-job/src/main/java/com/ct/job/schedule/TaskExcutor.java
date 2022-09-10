package com.ct.job.schedule;

import ch.qos.logback.classic.Logger;
import com.ct.job.config.JobConfig;
import com.ct.job.enums.NotifyCmd;
import com.ct.job.model.Node;
import com.ct.job.model.Task;
import com.ct.job.register.RegisterContext;
import com.ct.job.strategy.Strategy;
import lombok.SneakyThrows;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Component
public class TaskExcutor {
    private static final Logger logger= (Logger) LoggerFactory.getLogger(TaskExcutor.class);
    @Autowired
    private JobConfig config;
    //@Autowired
    //private TaskMapper taskMapper;
    //@Autowired
    //private NodeMapper nodeMapper;
    private Strategy strategy;
    private Map<Long,Future> doingFutures = new HashMap<>();
    private DelayQueue<DelayItem<Task>> taskQueue=new DelayQueue<>();

    private ExecutorService pool= Executors.newFixedThreadPool(2);
    private ThreadPoolExecutor workPool;



    @PostConstruct
    public void init(){
        config.setSysStartTime(new Date());
        strategy=Strategy.choose(config.getNodeStrategy());
        workPool=new ThreadPoolExecutor(config.getCorePoolSize(),config.getMaxPoolSize(),60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(config.getQueueSize()));
        pool.execute(new Load());
        pool.execute(new Boss());
    }

    class Load implements Runnable{
        @SneakyThrows
        @Override
        public void run(){
            for(;;){
                Thread.sleep(config.getFetchDuration());
                //获取可用节点
                List<Node> nodes= RegisterContext.chooseRegister().getEnableNodes(config.getHeartBeatSeconds()*2);
                //List<Node> nodes= nodeMapper.getEnableNodes(config.getHeartBeatSeconds()*2);
                if(nodes.size()==0) continue;

                //获取规定时间后将要执行的任务
                List<Task> tasks=RegisterContext.chooseRegister().getNotStartTask(config.getFetchDuration());
                //List<Task> tasks=taskMapper.getNotStartTask(config.getFetchDuration());
                if(tasks.size()==0) continue;

                //为任务分配执行的节点
                logger.info("------------------------获取到可执行的任务----------------------------------");
                for (Task task:tasks){
                    boolean accept=strategy.accept(nodes,task,config.getNodeId());
                    if(!accept) continue;
                    task.setNodeId(config.getNodeId());
                    int n=task.pending();
                    Date nextStartTime = task.getNextStartTime();
                    if(n==0 || nextStartTime==null){
                        continue;
                    }

                    /**
                     * 如果任务的下次启动时间还在系统启动时间之前，说明时间已过期需要重新更新
                     */

                    if(nextStartTime != null && nextStartTime.before(config.getSysStartTime())) {
                        /**
                         * 如果服务停止重新启动后由于之前的任务的nextStartTime时间还是之前的就可能存在，再次启动后仍然按照之前时间执行的情况
                         */
                        task.notStart();
                        continue;
                    }

                    /**
                     * 封装成延时对象放入延时队列,这里再查一次是因为上面乐观锁已经更新了版本，会导致后面结束任务更新不成功
                     */
                    task = RegisterContext.chooseRegister().get(task.getId());
                    //task = taskMapper.get(task.getId());
                    DelayItem<Task> delayItem = new DelayItem<Task>(nextStartTime.getTime() - new Date().getTime(), task);
                    System.out.println(delayItem);
                    taskQueue.offer(delayItem);


                }

            }
        }
    }

    class Boss implements Runnable{

        @SneakyThrows
        @Override
        public void run() {
            //到时间取出延时队列中的任务执行

            for (;;){
                DelayItem<Task> delayItem=taskQueue.take();
                if(delayItem !=null && delayItem.getItem()!=null){
                    logger.info("--------------------------------任务时间，准备开始执行任务------------------------------------");
                    Task task=delayItem.getItem();
                    task.doing();
                    Future future=workPool.submit(new Worker(task));
                    doingFutures.put(task.getId(),future);
                    future.get();
                }
            }
        }
    }

    class Worker implements Callable{
        private Task task;
        public Worker(Task task){
            this.task=task;
        }

        @Override
        public Object call() throws Exception {
            logger.info("-----------------------进入任务线程-----------------------");
            task.getInvokor().invoke();
            logger.info("------------------------------执行任务---------------------------");
            finish(task);
            logger.info("-------------------------------结束处理完成-----------------------");
            doingFutures.remove(task.getId());
            return null;

        }
    }

    public void finish(Task task) throws ParseException {
        task.setNextStartTime();
        logger.info("执行任务：{},下次执行时间：{}",task.getName(),task.getNextStartTime());
        /**
         *  如果没有下次执行时间了，该任务就完成了，反之变成未开始
         */
        int n=0;
        if(task.getNextStartTime() == null) {
            task.finish();
        } else {
            task.notStart();
        }

        /**
         * 使用乐观锁检测是否可以更新成功，成功则更新详情
         */

        if(n > 0) {
            RegisterContext.chooseRegister().addCounts(task.getTaskDetail().getNodeId());
            //nodeMapper.addCounts(task.getTaskDetail().getNodeId());
        }
    }

    public void failHandle(Task task){
        task.error();
    }

    public boolean stop(Long taskId) throws ParseException {
        Task task = RegisterContext.chooseRegister().get(taskId);
        //Task task = taskMapper.get(taskId);
        /**
         * 不是自己节点的任务，本节点不能执行停用
         */
        if(task == null || !config.getNodeId().equals(task.getNodeId())) {
            return false;
        }
        /**
         * 拿到正在执行任务的future，然后强制停用，并删除doingFutures的任务
         */
        Future future = doingFutures.get(taskId);
        boolean flag =  future.cancel(true);
        if(flag) {
            doingFutures.remove(taskId);
            /**
             * 修改状态为已停用
             */
            task.stop();
        }
        /**
         * 重置通知信息，避免重复执行停用通知
         */
        RegisterContext.chooseRegister().resetNotifyInfo(config.getNodeId(), NotifyCmd.STOP_TASK);
        //nodeMapper.resetNotifyInfo(config.getNodeId(),NotifyCmd.STOP_TASK);
        return flag;
    }
}

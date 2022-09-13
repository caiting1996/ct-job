package com.ct.job.model;

import com.ct.job.config.JobConfig;
import com.ct.job.register.Register;
import com.ct.job.register.RegisterContext;
import com.ct.job.state.*;
import com.ct.job.utils.CronExpression;
import com.ct.job.utils.SpringContextUtil;
import com.ct.job.enums.Invocation;
import com.ct.job.enums.TaskStatus;
import com.ct.job.serializer.JdkSerializationSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.ParseException;
import java.util.Date;

/**
 * 任务实体
 */
@Data
@Builder
@AllArgsConstructor

public class Task {

    private Long id;
    private boolean Recover=false;

    /**
     * 调度名称
     */
    private String name;

    /**
     * 分片
     */
    private int frag;



    /**
     * cron表达式
     */
    private String cronExpr;

    /**
     * 当前执行的节点id
     */
    private Long nodeId;

    /**
     * 状态，0表示未开始，1表示待执行，2表示执行中，3表示已完成
     */
    private int status = TaskStatus.NOT_STARTED.getId();

    /**
     * 成功次数
     */
    private Integer successCount;

    /**
     * 失败次数
     */
    private Integer failCount;

    /**
     * 执行信息
     */
    private byte[] invokeInfo;

    /**
     * 乐观锁标识
     */
    private Integer version;

    /**
     * 首次开始时间
     */
    private Date firstStartTime;

    /**
     * 下次开始时间
     */
    private Date nextStartTime;

    /**
     * 创建时间
     */
    private Date createTime = new Date();

    /**
     * 更新时间
     */
    private Date updateTime = new Date();

    /**
     * 任务的执行者
     */
    private Invocation invokor;
    private TaskDetail taskDetail;
    private TaskState noStartState=new NoStartState(this);
    private TaskState pendingState=new PendingState(this);
    private TaskState doingState=new DoingState(this);
    private TaskState errorState=new ErrorState(this);
    private TaskState finishState=new FinishState(this);
    private TaskState stopState=new StopState(this);
    private TaskState nowState=noStartState;
    //private TaskMapper taskMapper= (TaskMapper) SpringContextUtil.getBean(TaskMapper.class);
    private JobConfig config= (JobConfig) SpringContextUtil.getBean(JobConfig.class);
    private Register register= RegisterContext.chooseRegister();

    public Task() {
    }

    public Task(String name, String cronExpr, Invocation invokor) {
        this.name = name;
        this.cronExpr = cronExpr;
        this.invokor = invokor;
        this.invokeInfo=new JdkSerializationSerializer().serialize(invokor);
    }

    public Invocation getInvokor() {
        this.invokor=new JdkSerializationSerializer<Invocation>().deserialize(this.invokeInfo);
        return invokor;
    }
    public void setNextStartTime() throws ParseException {
        CronExpression cronExpession = new CronExpression(this.cronExpr);
        Date nextStartDate=null;
        if(nextStartTime != null && nextStartTime.before(config.getSysStartTime())){
            nextStartDate = cronExpession.getNextValidTimeAfter(config.getSysStartTime());
        }else {
            nextStartDate=cronExpession.getNextValidTimeAfter(nextStartTime);
        }
        this.nextStartTime=nextStartDate;

    }
    public Integer notStart() throws ParseException {return nowState.notStart();}
    public Integer pending(){
        return nowState.pending();
    }
    public boolean doing(){
        return nowState.doing();
    }
    public boolean error(){
        return nowState.error();
    }
    public Integer finish() throws ParseException {
        return nowState.finish();
    }
    public boolean stop() throws ParseException {
        return nowState.stop();
    }
}

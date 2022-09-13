package com.ct.job.annotation;

import ch.qos.logback.classic.Logger;
import com.ct.job.config.JobConfig;
import com.ct.job.enums.Invocation;
import com.ct.job.enums.TaskStatus;
import com.ct.job.mapper.TaskMapper;
import com.ct.job.model.Task;
import com.ct.job.model.TaskDetail;
import com.ct.job.register.RegisterContext;
import com.ct.job.utils.CronExpression;

import lombok.SneakyThrows;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.text.ParseException;
import java.util.*;


@Component
public class JobInitialnize implements ApplicationListener<ContextRefreshedEvent> {
    private static final Logger logger= (Logger) LoggerFactory.getLogger(JobInitialnize.class);
    private Map<String,Map> taskId=new HashMap();
    @Autowired
    private JobConfig config;

    @SneakyThrows
    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        logger.info("-------------开始加载定时任务----------------");
        config.setSysStartTime(new Date());
        RegisterContext.chooseRegister().deleteTask();
        RegisterContext.chooseRegister().deleteTaskDetail();
        if(contextRefreshedEvent.getApplicationContext().getParent()==null){
            ApplicationContext context=contextRefreshedEvent.getApplicationContext();
            Map beans=context.getBeansWithAnnotation(EnableScheduling.class);
            if(beans==null)
                return;
            Map methodMap=new HashMap();

            Map allBeans=context.getBeansWithAnnotation(Component.class);
            Set<Map.Entry<String,Object>> entrys=allBeans.entrySet();
            for ( Map.Entry entry:entrys){
                Object obj=entry.getValue();
                Class clazz=obj.getClass();
                Method[] methods=clazz.getMethods();
                for(Method method:methods){
                    if(method.isAnnotationPresent(Scheduled.class)){
                        methodMap.put(clazz.getName()+"."+method.getName(),method);
                    }
                }
            }
            if(methodMap.isEmpty()) {
                logger.info("未定义定时任务");
                return;
            }
            handleMethods(methodMap);
            logger.info("----------------------------------加载定时任务完成——————————————————————————————————————");

        }


    }

    public void handleMethods(Map methodMap) throws ParseException {
        Set<Map.Entry<String,Object>> methods=methodMap.entrySet();
        for (Map.Entry method:methods){
            handleEachMethod(methodMap, (Method) method.getValue());
        }
    }

    public void handleEachMethod(Map methodMap ,Method m) throws  ParseException {
        Class clazz=m.getDeclaringClass();
        String fileName=clazz.getName()+"."+m.getName();
        String methodName=m.getName();
        Parameter[] parameters=m.getParameters();


        Scheduled scheduled=m.getAnnotation(Scheduled.class);
        String corn=scheduled.corn();

        int frag=scheduled.frag();


        if(taskId==null || !taskId.containsKey(fileName) ){
            Map temp=new HashMap();
            for (int i=1;i<=frag;i++){
                Class[] classes=null;
                Object[] objects=null;
                if(parameters.length!=0){
                    classes=new Class[2];
                    objects=new Object[2];
                    for (int j=0;j< parameters.length;j++){
                        classes[j]=parameters[j].getType();
                    }
                    objects[0]=frag;
                    objects[1]=i;
                }else{
                    classes=new Class[]{};
                    objects=new Object[]{};
                }
                Task task=new Task(fileName,corn,new Invocation(clazz,methodName,classes, objects));
                CronExpression cronExpession = new CronExpression(task.getCronExpr());
                Date nextStartDate = cronExpession.getNextValidTimeAfter(new Date());
                task.setFirstStartTime(nextStartDate);
                task.setNextStartTime(nextStartDate);
                task.setFrag(i);
                task.setVersion(1);
                task.setSuccessCount(0);
                task.setFailCount(0);
                RegisterContext.chooseRegister().insertTask(task);
                temp.put(fileName+i,task.getId());
                taskId.put(fileName,temp);
                TaskDetail taskDetail=TaskDetail.builder()
                        .taskId(task.getId())
                        .status(TaskStatus.NOT_STARTED.getId())
                        .version(1)
                        .build();
                RegisterContext.chooseRegister().insertTaskDetail(taskDetail);
                }
            }
        }
}

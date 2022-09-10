package com.ct.job.enums;

import com.ct.job.utils.SpringContextUtil;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * 任务执行方法，用于序列化保存在数据库
 */
public class Invocation implements Serializable {

    private Class targetClass;

    private String methodName;

    private Class[] parameterTypes;

    private Object[] args;


    public Invocation() {

    }

    public Invocation(Class targetClass, String methodName, Class[] parameterTypes, Object... args) {
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
        this.targetClass = targetClass;
        this.args = args;
    }

    public Object[] getArgs() {
        return args;
    }

    public Class getTargetClass() {
        return targetClass;
    }

    public String getMethodName() {
        return methodName;
    }

    public Class[] getParameterTypes() {
        return parameterTypes;
    }

    public Object invoke() throws Exception {
        Object target = null;
        try {
            target = SpringContextUtil.getBean(targetClass);
        } catch(NoSuchBeanDefinitionException e) {
            target = Class.forName(targetClass.getName());
        }
        Method method = target.getClass().getMethod(methodName,parameterTypes);
        // 调用服务方法
        System.out.println(method.getParameters());
        return method.invoke(target, args);
    }
}

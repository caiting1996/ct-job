package com.ct.job.enums;

/**
 * 节点状态枚举类
 */
public enum NodeStatus {

    //待执行
    DISABLE(0),
    //执行中
    ENABLE(1);

    int id;

    NodeStatus(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public  static NodeStatus valueOf(int id) {
        switch (id) {
            case 1:
                return ENABLE;
            default:
                return DISABLE;
        }
    }

}

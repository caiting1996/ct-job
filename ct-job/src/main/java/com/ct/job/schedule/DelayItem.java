package com.ct.job.schedule;



import javax.validation.constraints.NotNull;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * 延时任务
 * @param <T>
 */
public class DelayItem<T> implements Delayed {
    private final long delay;
    private final long expire;
    private final T t;
    private final long now;
    public DelayItem(long delay,T t){
        this.delay=delay;
        this.t=t;
        this.expire=System.currentTimeMillis()+delay;
        this.now=System.currentTimeMillis();
    }

    public DelayItem(long expire,T t,long delay){
        this.delay=expire-System.currentTimeMillis();
        this.t=t;
        this.expire=expire;
        this.now=System.currentTimeMillis();
    }

    @Override
    public long getDelay(@NotNull TimeUnit unit) {

        return unit.convert(this.expire-System.currentTimeMillis(),TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(@NotNull Delayed o) {
        return (int) (this.getDelay(TimeUnit.MILLISECONDS)-o.getDelay(TimeUnit.MILLISECONDS));
    }

    public T getItem() {
        return t;
    }
}

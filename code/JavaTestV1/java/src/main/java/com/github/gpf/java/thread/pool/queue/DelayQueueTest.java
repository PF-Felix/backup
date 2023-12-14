package com.github.gpf.java.thread.pool.queue;

import lombok.Data;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

@Data
public class DelayQueueTest implements Delayed {

    /**
     * 任务名称
     */
    private String taskName;
    /**
     * 任务执行时间
     */
    private long currentMillSeconds;

    public static void main(String[] args) throws InterruptedException {
        DelayQueue<DelayQueueTest> queue = new DelayQueue<>();
        queue.add(new DelayQueueTest("任务1", 5000L));
        queue.add(new DelayQueueTest("任务2", 3000L));
        queue.add(new DelayQueueTest("任务3", 8000L));

        System.out.println(queue.take().getTaskName());
        System.out.println(queue.take().getTaskName());
        System.out.println(queue.take().getTaskName());
    }

    /**
     * @param taskName 任务名称
     * @param delayMillSeconds 延迟时间
     */
    public DelayQueueTest(String taskName, long delayMillSeconds) {
        this.taskName = taskName;
        this.currentMillSeconds = System.currentTimeMillis() + delayMillSeconds;
    }

    @Override
    public int compareTo(Delayed o) {
        return new Long((currentMillSeconds - ((DelayQueueTest)o).currentMillSeconds)).intValue();
    }

    /**
     * 设置延迟时间，每时每刻都在动态变化
     */
    @Override
    public long getDelay(TimeUnit unit) {
        return currentMillSeconds - System.currentTimeMillis();
    }
}

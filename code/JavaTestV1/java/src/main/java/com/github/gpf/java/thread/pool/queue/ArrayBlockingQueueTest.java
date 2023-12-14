package com.github.gpf.java.thread.pool.queue;

import java.util.concurrent.ArrayBlockingQueue;

public class ArrayBlockingQueueTest {
    public static void main(String[] args) throws InterruptedException {
        //必须指定队列长度，超过长度就会报错
        ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(3);
        queue.add("tt");
        queue.offer("tom");
        queue.put("jenny");
        queue.offer("tim");

        System.out.println(queue.peek());
        System.out.println(queue.remove());
        System.out.println(queue.poll());
        System.out.println(queue.take());
        System.out.println(queue.poll());
    }
}

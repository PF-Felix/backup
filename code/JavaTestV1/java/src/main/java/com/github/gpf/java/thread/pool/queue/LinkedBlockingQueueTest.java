package com.github.gpf.java.thread.pool.queue;

import java.util.concurrent.LinkedBlockingQueue;

public class LinkedBlockingQueueTest {
    public static void main(String[] args) {
        LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>(3);
        queue.add("1");
        queue.add("2");
        queue.add("3");
        queue.add("4");
    }
}

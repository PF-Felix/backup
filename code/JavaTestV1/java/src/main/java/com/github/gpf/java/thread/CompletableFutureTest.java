package com.github.gpf.java.thread;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

/**
 * CompletableFuture
 * 任务编排
 */
public class CompletableFutureTest {

    /**
     * runAsync 无返回值
     * supplyAsync 有返回值
     * 这两个方法都可以指定线程池，如果没有指定的话默认使用 ForkJoinPool
     */
    @Test
    public void testRunAsync() throws Exception {
        CompletableFuture.runAsync(() -> {
            System.out.println("任务1");
        });

        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务2");
            return "任务2的结果";
        });
        while (completableFuture.isDone()) {
            System.out.println(completableFuture.get());
            break;
        }
    }

    /**
     * accept 前置任务完成，就触发回调，回调能够得到前置任务的返回值
     * acceptAsync 能够指定线程池，如果不指定就用默认的线程池
     * thenRun thenRunAsync 同上 区别在于这个方法不能接收前置方法的返回值
     * thenApply thenApplyAsync 同上 区别在于这个方法能接收前置方法的返回值，自己也有返回值
     */
    @Test
    public void testAccept() throws Exception {
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(3000L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("任务1");
        }).thenAccept(c -> {
            System.out.println("任务2");
            System.out.println(c);
        }).get();

        System.out.println("=====");

        CompletableFuture.supplyAsync(() -> {
            System.out.println("任务1");
            return "任务1的结果";
        }).thenAccept(c -> {
            System.out.println("任务2");
            System.out.println(c);
        }).get();
    }

    /**
     * acceptEither 任何一个任务完成，就触发回调
     * acceptEitherAsync 能够指定线程池，如果不指定就用默认的线程池
     * runAfterEither applyToEither 是类似的
     *
     * 同理：runAfterBoth thenAcceptBoth thenCombine 同上
     */
    @Test
    public void testAcceptEither() throws Exception {
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(3000L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("任务1");
        }).acceptEither(
                CompletableFuture.runAsync(() -> {
                    try {
                        Thread.sleep(1000L);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("任务2");
                }),
                (c) -> {
                    System.out.println("任务3");
                    System.out.println(c);
                }
        ).get();

        System.out.println("=====");

        CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("任务1");
            return "任务1的结果";
        }).acceptEither(
                CompletableFuture.supplyAsync(() -> {
                    try {
                        Thread.sleep(3000L);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("任务2");
                    return "任务2的结果";
                }),
                (c) -> {
                    System.out.println("任务3");
                    System.out.println(c);
                }
        ).get();
    }

    /**
     * exceptionally thenCompose handle 可以用于处理异常
     *
     * allOf 的方式是让内部编写多个 CompletableFuture 的任务，多个任务都执行完后，才会继续执行后续拼接的任务；无返回值
     * anyOf 的方式是让内部编写多个 CompletableFuture 的任务，只要有一个前置任务执行完毕就继续执行后续拼接的任务
     */
}

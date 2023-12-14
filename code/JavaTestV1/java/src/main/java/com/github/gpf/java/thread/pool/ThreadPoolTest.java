package com.github.gpf.java.thread.pool;

import lombok.Data;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.*;

public class ThreadPoolTest {

    @Test
    public void testNewFixedThreadPool() throws IOException {
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        executorService.execute(() -> {
            System.out.println("任务1");
            try {
                Thread.sleep(5000L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        executorService.execute(() -> {
            System.out.println("任务2");
            try {
                Thread.sleep(5000L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        executorService.execute(() -> {
            System.out.println("任务3");
            try {
                Thread.sleep(5000L);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        executorService.execute(() -> {
            System.out.println("任务4");
        });

        System.in.read();
    }

    @Test
    public void testNewScheduleThreadPool() throws IOException, InterruptedException {
        ScheduledExecutorService service = Executors.newScheduledThreadPool(3);

        //正常执行
        service.execute(() -> {
            System.out.println(Thread.currentThread().getName());
        });

        //延迟5秒执行
        service.schedule(() -> {
            System.out.println(Thread.currentThread().getName());
        },5, TimeUnit.SECONDS);

        Thread.sleep(10000L);

        //周期执行，第一次延迟5s执行，然后每3s执行一次
        //本次任务开始时，间隔period的时间间隔就可以执行下一个任务了
        int period = 1;
//        int period = 4;
//        System.out.println(System.currentTimeMillis());
//        service.scheduleAtFixedRate(() -> {
//            try {
//                Thread.sleep(3000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            System.out.println(System.currentTimeMillis());
//        },2, period, TimeUnit.SECONDS);

        //周期执行，当前任务第一次延迟5s执行，然后没3s执行一次
        //本次任务结束后，间隔delay的时间才会执行下一个任务
        System.out.println(System.currentTimeMillis());
        service.scheduleWithFixedDelay(() -> {
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(System.currentTimeMillis());
        },2, 1, TimeUnit.SECONDS);

//        System.in.read();
    }

    @Test
    public void testNewWorkStealingPool() throws IOException {
        int[] nums = new int[500000000];
        for (int i = 0; i < nums.length; i++) {
            nums[i] = (int) ((Math.random()) * 1000);
        }

        System.out.println("单线程计算数组总和");
        long start = System.nanoTime();
        int sum = 0;
        for (int num : nums) {
            sum += num;
        }
        long end = System.nanoTime();
        System.out.println("单线程运算结果为：" + sum + "，计算时间为：" + (end  - start));

        ForkJoinPool forkJoinPool = (ForkJoinPool) Executors.newWorkStealingPool();
        System.out.println("分而治之计算数组总和");
        long forkJoinStart = System.nanoTime();
        ForkJoinTask<Integer> task = forkJoinPool.submit(new SumRecursiveTask(nums, 0, nums.length - 1));
        Integer result = task.join();
        long forkJoinEnd = System.nanoTime();
        System.out.println("分而治之运算结果为：" + result + "，计算时间为：" + (forkJoinEnd  - forkJoinStart));
    }
}

@Data
class SumRecursiveTask extends RecursiveTask<Integer>{
    private int[] nums;
    private int start;
    private int end;
    private final int MAX_STRIDE = 50000000;

    public SumRecursiveTask(int[] nums, int start, int end) {
        this.nums = nums;
        this.start = start;
        this.end = end;
    }

    @Override
    protected Integer compute() {
        int sum = 0;
        int stride = end - start;
        if(stride <= MAX_STRIDE){
            for (int i = start; i <= end; i++) {
                sum += nums[i];
            }
        } else {
            //将任务拆分为两个任务
            int middle = (start + end) / 2;
            SumRecursiveTask left = new SumRecursiveTask(nums, start, middle);
            SumRecursiveTask right = new SumRecursiveTask(nums, middle + 1, end);
            //分别执行
            left.fork();
            right.fork();
            sum = left.join() + right.join();
        }
        return sum;
    }
}

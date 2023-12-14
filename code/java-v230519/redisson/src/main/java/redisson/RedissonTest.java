package redisson;

import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RSemaphore;

public class RedissonTest {

    /**
     * 信号量
     */
    @Test
    public void testRSemaphore() {
        Redisson redisson = RedissonManager.getRedisson();

        //获得信号量
        RSemaphore semaphore = redisson.getSemaphore("testRSemaphore1");

        String availablePermits = "剩余令牌数量:";
        System.out.println(availablePermits + semaphore.availablePermits());

        //设置令牌数量 原子性的
        System.out.println("设置令牌数量100:" + semaphore.trySetPermits(100));
        System.out.println("设置令牌数量80:" + semaphore.trySetPermits(80));

        System.out.println(availablePermits + semaphore.availablePermits());

        //消耗一个令牌 原子性的
        if (semaphore.tryAcquire()) {
            System.out.println("消耗了1个令牌成功");
        } else {
            System.out.println("消耗了1个令牌失败");
        }
        System.out.println(availablePermits + semaphore.availablePermits());

        if (semaphore.tryAcquire(5)) {
            System.out.println("消耗了5个令牌成功");
        } else {
            System.out.println("消耗了5个令牌失败");
        }
        System.out.println(availablePermits + semaphore.availablePermits());

        if (semaphore.tryAcquire(94)) {
            System.out.println("消耗了94个令牌成功");
        } else {
            System.out.println("消耗了94个令牌失败");
        }
        System.out.println(availablePermits + semaphore.availablePermits());

        if (semaphore.tryAcquire()) {
            System.out.println("消耗了1个令牌成功");
        } else {
            System.out.println("消耗了1个令牌失败");
        }
        System.out.println(availablePermits + semaphore.availablePermits());

        semaphore.release();
        System.out.println("补充了1个令牌:");
        System.out.println(availablePermits + semaphore.availablePermits());

        if (semaphore.tryAcquire()) {
            System.out.println("消耗了1个令牌成功");
        } else {
            System.out.println("消耗了1个令牌失败");
        }
        System.out.println(availablePermits + semaphore.availablePermits());

        if (semaphore.tryAcquire()) {
            System.out.println("消耗了1个令牌成功");
        } else {
            System.out.println("消耗了1个令牌失败");
        }
        System.out.println(availablePermits + semaphore.availablePermits());
    }
}

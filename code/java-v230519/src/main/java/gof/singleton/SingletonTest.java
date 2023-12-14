package gof.singleton;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 单例模式
 *
 * 线程安全的方式：
 *     1. 饿汉式
 *     2. 懒汉式 + 双重检查 + volatile
 *     3. 静态内部类
 *
 * 选择方案：1 or 3
 *
 * 单例模式的应用：
 *     1. Spring
 */
public class SingletonTest {

    @Test
    public void test() {
        Assertions.assertSame(Singleton1.getInstance(), Singleton1.getInstance());
        Assertions.assertSame(Singleton2.getInstance(), Singleton2.getInstance());
        Assertions.assertSame(Singleton3.getInstance(), Singleton3.getInstance());
        Assertions.assertSame(Singleton4.getInstance(), Singleton4.getInstance());
        Assertions.assertSame(Singleton5.getInstance(), Singleton5.getInstance());
        Assertions.assertSame(Singleton6.getInstance(), Singleton6.getInstance());
        Assertions.assertSame(Singleton7.getInstance(), Singleton7.getInstance());
        Assertions.assertSame(Singleton8.getInstance(), Singleton8.getInstance());
    }
}

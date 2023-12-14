package gof.singleton;

/**
 * 饿汉式单例
 * 类加载时就完成实例化，线程安全
 */
public class Singleton1 {
    private static final Singleton1 instance = new Singleton1();

    public static Singleton1 getInstance() {
        return instance;
    }

    private Singleton1() {
    }
}

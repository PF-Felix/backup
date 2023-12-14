package gof.singleton;

/**
 * 双重检查 + volatile，实现线程安全
 * volatile 禁止指令重排序，保证了对象赋值给一个引用之时已经被完全初始化
 */

public class Singleton7 {
    private volatile static Singleton7 instance = null;

    public static Singleton7 getInstance() {
        if (instance == null) {
            synchronized (Singleton7.class) {
                if (instance == null) {
                    instance = new Singleton7();
                }
            }
        }
        return instance;
    }

    private Singleton7() {
    }
}

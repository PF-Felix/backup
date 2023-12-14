package gof.singleton;

/**
 * 双重检查，第一次检查有助于提升效率
 *     第二次检查解决了可能得到两个对象的问题
 *     但仍然非线程安全：可能得到一个尚未初始化完全的对象
 */
public class Singleton6 {
    private static Singleton6 instance = null;

    public static Singleton6 getInstance() {
        if (instance == null) {
            synchronized (Singleton6.class) {
                if (instance == null) {
                    instance = new Singleton6();
                }
            }
        }
        return instance;
    }

    private Singleton6() {
    }
}

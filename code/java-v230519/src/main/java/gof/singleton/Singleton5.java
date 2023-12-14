package gof.singleton;

/**
 * 进一步缩小 synchronized 代码块
 * 又回到了线程安全问题上来了：前面的两个问题依然存在
 */
public class Singleton5 {
    private static Singleton5 instance = null;

    public static Singleton5 getInstance() {
        if (instance == null) {
            synchronized (Singleton5.class) {
                instance = new Singleton5();
            }
        }
        return instance;
    }

    private Singleton5() {
    }
}

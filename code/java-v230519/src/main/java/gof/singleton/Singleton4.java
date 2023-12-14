package gof.singleton;

/**
 * 缩小 synchronized 代码块
 * 对于提高效率微乎其微
 */
public class Singleton4 {
    private static Singleton4 instance = null;

    public static Singleton4 getInstance() {
        synchronized (Singleton4.class) {
            if (instance == null) {
                instance = new Singleton4();
            }
        }
        return instance;
    }

    private Singleton4() {
    }
}

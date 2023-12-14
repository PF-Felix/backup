package gof.singleton;

/**
 * 懒汉式单例的安全问题可以使用 synchronized 解决
 * 但阻塞导致效率低
 */
public class Singleton3 {
    private static Singleton3 instance = null;

    synchronized public static Singleton3 getInstance(){
        if(instance == null){
            instance = new Singleton3();
        }
        return instance;
    }

    private Singleton3() {
    }
}

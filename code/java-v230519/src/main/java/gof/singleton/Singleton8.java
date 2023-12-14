package gof.singleton;

/**
 * 静态内部类的方式，既有懒汉式懒的优点，又有饿汉式线程安全的优点，而且不会像双重检查那样牺牲效率
 * 内部类加载的时候饿汉式初始化
 */
public class Singleton8 {
    private static class Holder {
        public static Singleton8 instance = new Singleton8();
    }

    public static Singleton8 getInstance() {
        return Holder.instance;
    }

    private Singleton8() {
    }
}

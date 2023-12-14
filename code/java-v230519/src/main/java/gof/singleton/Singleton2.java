package gof.singleton;

/**
 * 懒汉式单例
 * 在需要的时候实例化
 * 但是：带来了线程安全问题
 *     两个线程可能得到两个不同的对象
 *     可能得到一个尚未完全初始化的对象
 * new对象，分为三个事情：1开辟内存空间 2初始化内部属性 3将地址给予instance引用
 * 因为指令重排，可能会将原有的123顺序，修改为132
 */
public class Singleton2 {
    private static Singleton2 instance = null;

    public static Singleton2 getInstance(){
        if(instance == null){
            instance = new Singleton2();
        }
        return instance;
    }

    private Singleton2() {
    }
}

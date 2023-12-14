package gof.factory.code3;

/**
 * 工厂方法模式用于连接平行的类层次
 */
public class Client {
    public static void main(String[] args) {
        AbstractFactory factory = new FactoryImpl();
        factory.operate(2);
        factory.operate(1);
        factory.operate(0);
        factory.operate(999);
    }
}

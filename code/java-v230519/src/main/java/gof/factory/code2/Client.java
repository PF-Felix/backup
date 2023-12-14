package gof.factory.code2;

public class Client {
    public static void main(String[] args) {
        AbstractFactory factory = new AbstractFactory();
        factory.operate(1);
        factory.operate(2);
        factory.operate(999);

        factory = new FactoryImpl();
        factory.operate(1);
        factory.operate(2);
        factory.operate(999);
    }
}

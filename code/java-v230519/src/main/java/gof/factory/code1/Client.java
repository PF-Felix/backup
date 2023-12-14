package gof.factory.code1;

public class Client {
    public static void main(String[] args) {
        AbstractFactory factory = new FactoryImpl();
        factory.operate(1);
        factory.operate(2);
        factory.operate(999);
        factory.operate(null);
    }
}

package gof.factory;

public class Product1 implements IProduct {
    @Override
    public String info() {
        return "产品1";
    }

    @Override
    public int price() {
        return 1;
    }
}

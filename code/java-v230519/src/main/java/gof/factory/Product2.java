package gof.factory;

public class Product2 implements IProduct {
    @Override
    public String info() {
        return "产品2";
    }

    @Override
    public int price() {
        return 2;
    }
}

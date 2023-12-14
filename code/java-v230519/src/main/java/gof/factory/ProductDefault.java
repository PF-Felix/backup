package gof.factory;

public class ProductDefault implements IProduct {
    @Override
    public String info() {
        return "试用产品是免费的哦";
    }

    @Override
    public int price() {
        return 0;
    }
}

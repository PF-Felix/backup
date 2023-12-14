package gof.factory.code3;

import gof.factory.IProduct;

public class ShopBarImpl1 extends AbstractShopBar {

    public ShopBarImpl1(IProduct product) {
        super(product);
    }

    @Override
    String name() {
        return "展示位1";
    }
}

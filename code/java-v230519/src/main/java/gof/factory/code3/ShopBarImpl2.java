package gof.factory.code3;

import gof.factory.IProduct;

public class ShopBarImpl2 extends AbstractShopBar {

    public ShopBarImpl2(IProduct product) {
        super(product);
    }

    @Override
    String name() {
        return "展示位2";
    }
}

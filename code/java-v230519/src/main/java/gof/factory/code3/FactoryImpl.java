package gof.factory.code3;

import gof.factory.IProduct;
import gof.factory.Product1;
import gof.factory.Product2;
import gof.factory.ProductChooser;

public class FactoryImpl extends AbstractFactory {

    @Override
    protected IProduct createProduct(Integer type) {
        return ProductChooser.choose(type);
    }

    @Override
    protected AbstractShopBar chooseShopBar(IProduct product) {
        // 为商品选择展示位
        if (product instanceof Product1) {
            return new ShopBarImpl1(product);
        } else if (product instanceof Product2) {
            return new ShopBarImpl2(product);
        }
        return null;
    }
}

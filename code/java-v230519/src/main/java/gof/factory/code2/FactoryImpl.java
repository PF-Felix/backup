package gof.factory.code2;

import gof.factory.IProduct;
import gof.factory.ProductChooser;

public class FactoryImpl extends AbstractFactory {

    @Override
    protected IProduct createProduct(Integer type) {
        return ProductChooser.choose(type);
    }
}

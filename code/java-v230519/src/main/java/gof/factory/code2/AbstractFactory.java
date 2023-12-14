package gof.factory.code2;

import gof.factory.IProduct;
import gof.factory.ProductDefault;

public class AbstractFactory {
    protected IProduct createProduct(Integer type){
        return new ProductDefault();
    }

    public void operate(Integer type){
        IProduct product = createProduct(type);
        System.out.println(product.info());
    }
}

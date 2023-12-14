package gof.factory.code1;

import gof.factory.IProduct;

public abstract class AbstractFactory {
    /**
     * 生产产品
     */
    abstract protected IProduct createProduct(Integer type);

    public void operate(Integer type){
        IProduct product = createProduct(type);
        System.out.println(product.info());
    }
}

package gof.factory.code3;

import gof.factory.IProduct;

/**
 * 商品位，一个商品位展示一个产品
 */
public abstract class AbstractShopBar {

    private IProduct product;

    public AbstractShopBar(IProduct product) {
        this.product = product;
    }

    public void sell() {
        System.out.println(name() + "：" + product.info() + "，售价" + product.price() + "RMB");
    }

    /**
     * 展示位名称
     */
    abstract String name();
}

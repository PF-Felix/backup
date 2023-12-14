package gof.factory.code3;

import gof.factory.IProduct;

public abstract class AbstractFactory {
    /**
     * 生产产品
     */
    abstract protected IProduct createProduct(Integer type);

    /**
     * 选择商品展示位
     */
    abstract protected AbstractShopBar chooseShopBar(IProduct product);

    public void operate(Integer type){
        IProduct product = createProduct(type);
        AbstractShopBar shopBar = chooseShopBar(product);
        if (shopBar == null) {
            System.out.println("没有合适的展示位售卖此商品");
        } else {
            shopBar.sell();
        }
    }
}

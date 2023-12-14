package gof.factory;

import java.util.Objects;

/**
 * 为了节省代码使用，真实情况可以去掉这个类
 */
public class ProductChooser {
    public static IProduct choose(Integer type){
        if (Objects.isNull(type)) {
            return new ProductDefault();
        }

        switch (type) {
            case 1: return new Product1();
            case 2: return new Product2();
            default: return new ProductDefault();
        }
    }
}

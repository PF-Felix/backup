package com.github.gpf.spring.sourcecode.bean.factoryBean;

import lombok.Data;
import org.springframework.beans.factory.FactoryBean;

@Data
public class CarFactoryBean implements FactoryBean<Car> {
    private String carInfo;

    @Override
    public boolean isSingleton() {
        return FactoryBean.super.isSingleton();
    }

    @Override
    public Car getObject() throws Exception {
        Car car = new Car();
        String[] infos = carInfo.split(",");
        car.setMaxSpeed(Integer.valueOf(infos[0]));
        car.setPrice(Double.valueOf(infos[1]));
        return car;
    }

    @Override
    public Class<Car> getObjectType() {
        return Car.class;
    }
}

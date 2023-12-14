package com.github.gpf.java.spi;

import java.sql.Driver;
import java.util.ServiceLoader;

/**
 * JAVA SPI
 */
public class SpiTest {
    public static void main(String[] args) {
        ServiceLoader<Driver> loader = ServiceLoader.load(Driver.class);
        for (Driver item : loader) {
            System.out.println("Get class:" + item.getClass());
        }
    }
}

package com.github.gpf.java.lombok;

import lombok.Synchronized;

/**
 * Synchronized 注解等同于同名关键字
 */

public class Synchronized1 {

    @Synchronized
    public void test(){
        System.out.println("test");
    }

    public static void main(String[] args) {
        System.out.println("哈哈");
    }
}

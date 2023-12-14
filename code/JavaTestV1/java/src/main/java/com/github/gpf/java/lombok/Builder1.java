package com.github.gpf.java.lombok;

import lombok.Builder;

/**
 * 链式构建对象
 */

@Builder
public class Builder1 {
    private String name;
    private int age;

    public static void main(String[] args) {
        Builder1Builder builder = Builder1.builder();
        Builder1 fuck = builder.age(1).name("fuck").build();
        System.out.println(fuck.name);
    }
}

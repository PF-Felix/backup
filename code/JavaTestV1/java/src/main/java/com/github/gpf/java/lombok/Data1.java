package com.github.gpf.java.lombok;

import lombok.Data;

/**
 * Data：众多注解的集合体
 * Getter
 * Setter
 * ToString
 * EqualsAndHashCode
 * RequiredArgsConstructor 生成一个空构造器
 */

@Data
public class Data1 {
    private String name;
    private int age;

    public static void main(String[] args) {

    }
}

package com.github.gpf.java.lombok;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Accessors：链式 setter
 */

@Setter
@Getter
@Accessors(chain = true)
public class Accessors1 {
    private String name;
    private int age;

    public static void main(String[] args) {
        Accessors1 accessors1 = new Accessors1();
        accessors1.setAge(1).setName("fuck");
    }
}

package com.github.gpf.java.util;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MyStringUtilsTest {

    @Test
    public void isEmpty() {
        Assertions.assertTrue(StringUtils.isEmpty(null));
        Assertions.assertTrue(StringUtils.isEmpty(""));
        Assertions.assertFalse(StringUtils.isEmpty(" "));

        Assertions.assertTrue(StringUtils.isEmpty(StringUtils.EMPTY));
        Assertions.assertFalse(StringUtils.isEmpty(StringUtils.SPACE));
    }

    @Test
    public void isBlank() {
        Assertions.assertTrue(StringUtils.isBlank(null));
        Assertions.assertTrue(StringUtils.isBlank(""));
        Assertions.assertTrue(StringUtils.isBlank(" "));
        Assertions.assertTrue(StringUtils.isBlank("  "));
        Assertions.assertTrue(StringUtils.isBlank("   "));
        Assertions.assertTrue(StringUtils.isBlank(StringUtils.EMPTY));
        Assertions.assertTrue(StringUtils.isBlank(StringUtils.SPACE));
    }

    @Test
    public void equals() {
        Assertions.assertTrue(StringUtils.equals(null, null));
        Assertions.assertFalse(StringUtils.equals(null, ""));
        Assertions.assertFalse(StringUtils.equals("", null));
        Assertions.assertTrue(StringUtils.equals("", ""));
        Assertions.assertFalse(StringUtils.equals("", " "));
    }
}

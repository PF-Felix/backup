package com.github.gpf.java.util;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

public class MyCollectionsUtilsTest {

    @Test
    public void isEmpty() {
        Assertions.assertTrue(CollectionUtils.isEmpty(null));
        Assertions.assertTrue(MapUtils.isEmpty(null));

        List<String> list = new LinkedList<>();
        Assertions.assertTrue(CollectionUtils.isEmpty(list));
        Collections.addAll(list, "1", "2", "3");
        Assertions.assertTrue(CollectionUtils.isNotEmpty(list));

        Map<String, String> map = new HashMap<>();
        Assertions.assertTrue(MapUtils.isEmpty(map));
        map.put("1", "1");
        Assertions.assertTrue(MapUtils.isNotEmpty(map));
    }
}

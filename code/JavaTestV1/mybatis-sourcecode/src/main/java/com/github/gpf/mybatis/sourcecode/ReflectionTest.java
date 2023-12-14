package com.github.gpf.mybatis.sourcecode;

import com.github.gpf.mybatis.sourcecode.entity.Student;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaClass;
import org.apache.ibatis.reflection.Reflector;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;

/**
 * 反射工具箱的使用
 */
public class ReflectionTest {

    /**
     * Reflector的使用
     */
    @Test
    public void reflectorTest() throws Exception {
        ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
        Reflector reflector = reflectorFactory.findForClass(Student.class);
        System.out.println("Class类型：" + reflector.getType());
        System.out.println("age类型：" + reflector.getGetterType("age"));
        System.out.println("age类型：" + reflector.getSetterType("age"));
        System.out.println("所有可读属性：" + Collections.addAll(new ArrayList<>(), reflector.getGetablePropertyNames()));
        System.out.println("所有可写属性：" + Collections.addAll(new ArrayList<>(), reflector.getSetablePropertyNames()));

        Student student = (Student) reflector.getDefaultConstructor().newInstance();
        reflector.getSetInvoker("name").invoke(student, new Object[]{"Tom"});
        reflector.getSetInvoker("age").invoke(student, new Object[]{18});
        //反射可以修改final属性
        reflector.getSetInvoker("birth").invoke(student, new Object[]{LocalDate.of(2000, 1, 1)});
        reflector.getSetInvoker("sex").invoke(student, new Object[]{"男"});
        System.out.println(student);

        Assertions.assertEquals("Tom", reflector.getGetInvoker("name").invoke(student, null));
        Assertions.assertEquals(18, (int) reflector.getGetInvoker("age").invoke(student, null));
        Assertions.assertTrue(reflector.hasGetter("name"));
        Assertions.assertFalse(reflector.hasSetter("friends[0]"));
    }

    /**
     * MetaClass可以检查复杂属性
     */
    @Test
    public void metaClassTest() {
        MetaClass metaClass = MetaClass.forClass(Student.class, new DefaultReflectorFactory());
        Assertions.assertTrue(metaClass.hasSetter("friend.name"));
        Assertions.assertTrue(metaClass.hasSetter("friends[0]"));
    }
}

package com.github.gpf.spring.sourcecode;

import com.github.gpf.spring.sourcecode.bean.aop.Baby;
import com.github.gpf.spring.sourcecode.bean.factoryBean.Car;
import com.github.gpf.spring.sourcecode.bean.factoryMethod.Student;
import com.github.gpf.spring.sourcecode.bean.lookupMethod.Dog;
import com.github.gpf.spring.sourcecode.bean.lookupMethod.Tom;
import com.github.gpf.spring.sourcecode.bean.rootBeanDefinition.Son;
import com.github.gpf.spring.sourcecode.bean.supplier.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 源码阅读类
 */
public class SpringSourcecodeTest {

    /**
     * FactoryBean的使用
     * 如果一个bean是FactoryBean，getBean就会调用其getObject得到真正的对象
     * 加上前缀获得的是FactoryBean对象
     *     getBean("&car") getBean("&&&car")获得的是同一个对象
     * 为什么有FactoryBean接口：实例化复杂的bean如果在配置文件里面写的话比较复杂，此时使用FactoryBean比较灵活
     */
    @Test
    public void test1() {
        ApplicationContext context = new ClassPathXmlApplicationContext("com/github/gpf/spring/sourcecode/beansV2.xml");
        Car car = (Car) context.getBean("car");
        System.out.println(car);
        System.out.println(car.getClass());
        System.out.println(context.getBean("&car").getClass());
        System.out.println(context.getBean("&&&car").getClass());
        Assertions.assertEquals(context.getBean("&car").hashCode(), context.getBean("&&&car").hashCode());
    }

    /**
     * 如果单例引用了原型作用域的bean，则这个原型作用域的bean也会被缓存，而不是每次都创建一个，这就违背了原型bean的初衷
     * 可以使用lookup-method解决这个问题
     */
    @Test
    public void test2() {
        ApplicationContext context = new ClassPathXmlApplicationContext("com/github/gpf/spring/sourcecode/beansV2.xml");
        Dog dog1 = (Dog) context.getBean("dog");
        Dog dog2 = (Dog) context.getBean("dog");
        Assertions.assertNotSame(dog1, dog2);
        Tom tom1 = (Tom) context.getBean("tom1");
        Tom tom2 = (Tom) context.getBean("tom1");
        Assertions.assertSame(tom1, tom2);
        Assertions.assertSame(tom1.getDog(), tom2.getDog());

        System.out.println("==============");

        tom1 = (Tom) context.getBean("tom2");
        tom2 = (Tom) context.getBean("tom2");
        Assertions.assertSame(tom1, tom2);
        Assertions.assertNotSame(tom1.getDog(), tom2.getDog());
    }

    /**
     * Supplier的使用场景应该与FactoryBean类似，都可以用来创建复杂对象
     */
    @Test
    public void test3() {
        ApplicationContext context = new ClassPathXmlApplicationContext("com/github/gpf/spring/sourcecode/beansV2.xml");
        User user1 = (User) context.getBean("user");
        User user2 = (User) context.getBean("user");
        System.out.println(user1);
        System.out.println(user2);
    }

    /**
     * spring对有父子关系的bean定义进行合并，子类对象能够得到父类对象的属性
     * RootBeanDefinition
     */
    @Test
    public void test4() {
        ApplicationContext context = new ClassPathXmlApplicationContext("com/github/gpf/spring/sourcecode/beansV2.xml");
        Son son = (Son) context.getBean("son");
        System.out.println(son.getName());
    }

    /**
     * 测试factory-method，与Supplier FactoryBean用法类似
     */
    @Test
    public void test5() {
        ApplicationContext context = new ClassPathXmlApplicationContext("com/github/gpf/spring/sourcecode/beansV2.xml");
        Student student1 = (Student) context.getBean("student");
        Student student2 = (Student) context.getBean("student");
        System.out.println(student1);
        System.out.println(student2);
    }

    /**
     * AOP测试
     */
    @Test
    public void test6() {
        ApplicationContext context = new ClassPathXmlApplicationContext("com/github/gpf/spring/sourcecode/beans.xml");
        Baby baby = (Baby) context.getBean("baby");
        baby.test();
        System.out.println("==========================");
        baby.testV2();
    }
}

package com.github.gpf.spring.sourcecode;

import com.github.gpf.spring.sourcecode.processor.MyXBeanFactoryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class MyApplicationContext extends ClassPathXmlApplicationContext {

    public MyApplicationContext(String... configLocation) {
        super(configLocation);
    }

    /**
     * 扩展点
     */
    @Override
    protected void initPropertySources() {
//        this.getEnvironment().setRequiredProperties("abc");
    }

    @Override
    protected void customizeBeanFactory(DefaultListableBeanFactory beanFactory) {
        super.setAllowBeanDefinitionOverriding(true);
        addBeanFactoryPostProcessor(new MyXBeanFactoryPostProcessor());
    }
}

package com.github.gpf.spring.sourcecode.processor;

import com.github.gpf.spring.sourcecode.bean.supplier.User;
import com.github.gpf.spring.sourcecode.bean.supplier.UserSupplier;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;

public class SupplierBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        System.out.println("SupplierBeanFactoryPostProcessor");

        BeanDefinition user = beanFactory.getBeanDefinition("user");
        GenericBeanDefinition beanDefinition = (GenericBeanDefinition) user;
        beanDefinition.setInstanceSupplier(UserSupplier::newUser);
        beanDefinition.setBeanClass(User.class);
    }
}

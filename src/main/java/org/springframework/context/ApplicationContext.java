package org.springframework.context;

import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.core.io.ResourceLoader;

/**
 * 应用上下文
 * 源码中的ApplicationContext接口继承了ListableBeanFactory，简介继承了BeanFactory接口。区别就是在BeanFactory接口之上扩展了
 *      一些getBean方法
 */
public interface ApplicationContext extends ListableBeanFactory, HierarchicalBeanFactory, ResourceLoader, ApplicationEventPublisher {

}

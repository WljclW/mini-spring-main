package org.springframework.beans.factory.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ConfigurableListableBeanFactory;

/**
 * 允许自定义修改BeanDefinition的属性值
 * 目的：允许对bean定义的修改
 * 执行时机：在BeanDefinition被加载后，但是在实际创建之前执行
 * 作用：修改bean的属性、添加属性值或者修改Bean的定义。常用于环境变量的注入、改变bean的作用域
 *
 */
public interface BeanFactoryPostProcessor {

	/**
	 * 在所有BeanDefintion加载完成后，但在bean实例化之前，提供修改BeanDefinition属性值的机制
	 *
	 * @param beanFactory
	 * @throws BeansException
	 */
	void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException;

}

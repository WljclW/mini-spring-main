package org.springframework.beans.factory.support;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;

/**
 * BeanDefinition注册表接口
 *	作用是提供一种机制来注册和管理Bean定义。具体来说：
 * 功能：允许开发者在运行时动态地注册、修改或删除Bean定义，而不仅仅依赖于静态的配置文件（如XML、注解等）。
 * 用途：主要用于自定义Bean定义的加载逻辑，支持程序化地向Spring容器中添加Bean定义。
 * 示例场景
 * 动态注册Bean：可以在应用程序运行时根据某些条件动态注册Bean定义，而不必重启应用或修改配置文件。
 * 自定义加载器：可以实现自定义的Bean定义加载逻辑，例如从数据库或网络加载Bean定义。
 * 模块化配置：可以在不同模块之间共享和注册Bean定义，增强模块间的解耦和灵活性。
 */
public interface BeanDefinitionRegistry {

	/**
	 * 向注册表中注BeanDefinition
	 *
	 * @param beanName
	 * @param beanDefinition
	 */
	void registerBeanDefinition(String beanName, BeanDefinition beanDefinition);

	/**
	 * 根据名称查找BeanDefinition
	 *
	 * @param beanName
	 * @return
	 * @throws BeansException 如果找不到BeanDefintion
	 */
	BeanDefinition getBeanDefinition(String beanName) throws BeansException;

	/**
	 * 是否包含指定名称的BeanDefinition
	 *
	 * @param beanName
	 * @return
	 */
	boolean containsBeanDefinition(String beanName);

	/**
	 * 返回定义的所有bean的名称
	 *
	 * @return
	 */
	String[] getBeanDefinitionNames();
}

package org.springframework.beans.factory;

import org.springframework.beans.BeansException;

/**
 * bean容器。也可以将BeanFactory视为spring容器的入口，通过它拿到ioc容器中的东西(比如通过BeanFactory来获取容器中
 * 		的bean、bean的属性等)。同时ApplicationContext扩展了这个接口，提供了更多的功能。
 *
 */
public interface BeanFactory {

	/**
	 * 获取bean
	 *
	 * @param name
	 * @return
	 * @throws BeansException bean不存在时
	 */
	Object getBean(String name) throws BeansException;

	/**
	 * 根据名称和类型查找bean
	 *
	 * @param name
	 * @param requiredType
	 * @param <T>
	 * @return
	 * @throws BeansException
	 */
	<T> T getBean(String name, Class<T> requiredType) throws BeansException;

	<T> T getBean(Class<T> requiredType) throws BeansException;

	boolean containsBean(String name);
}

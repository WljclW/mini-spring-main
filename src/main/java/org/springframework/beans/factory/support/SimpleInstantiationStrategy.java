package org.springframework.beans.factory.support;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;

import java.lang.reflect.Constructor;

/**
 主要作用：
 实例化策略：它是 InstantiationStrategy 接口的一个实现类，用于创建新的 bean 实例。它提供了最基础的实例化逻辑。
 无参构造函数实例化：它通过反射调用无参构造函数来创建 bean 实例。如果类没有无参构造函数，则会抛出异常。
 工厂方法实例化：它还支持通过静态工厂方法或实例工厂方法来创建 bean 实例。
 简化实例化过程：它提供了一种简单的、默认的实例化机制，适用于大多数场景。对于更复杂的实例化需求，Spring 还提供了其他实现（如 CGLIB 代理等）。
 */
public class SimpleInstantiationStrategy implements InstantiationStrategy {

	/**
	 * 简单的bean实例化策略，根据bean的无参构造函数实例化对象。
	 *
	 * @param beanDefinition
	 * @return
	 * @throws BeansException
	 */
	@Override
	public Object instantiate(BeanDefinition beanDefinition) throws BeansException {
		Class beanClass = beanDefinition.getBeanClass();
		try {	//通过BeanDefinition拿到对应的Class，通过反射拿到无参构造器创建对象
			Constructor constructor = beanClass.getDeclaredConstructor();
			return constructor.newInstance();
		} catch (Exception e) {
			throw new BeansException("Failed to instantiate [" + beanClass.getName() + "]", e);
		}
	}
}

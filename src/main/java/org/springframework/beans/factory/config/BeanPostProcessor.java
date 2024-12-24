package org.springframework.beans.factory.config;

import org.springframework.beans.BeansException;

/**
 * 用于修改实例化后的bean的修改扩展点
 *	如果我们需要在Spring 容器完成 Bean 的实例化、配置和其他的初始化前后添加一些自己的逻辑处理，我们就可以定义一个或者多
 *	个 BeanPostProcessor 接口的实现，然后注册到容器中。
 *目的：用于对Bean实例进行处理，允许在Bean实例化之后，依赖注入之前 或者 依赖注入之后进行一些操作
 *执行时机：在每一个Bean被实例化之后会执行该接口中的两个方法
 *使用场景：可以用来进行 Bean 的包装、修改属性、添加代理、记录日志等。
 *
 */
public interface BeanPostProcessor {

	/**
	 * 在bean执行初始化方法之前执行此方法
	 *
	 * @param bean
	 * @param beanName
	 * @return
	 * @throws BeansException
	 */
	Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException;

	/**
	 * 在bean执行初始化方法之后执行此方法
	 *
	 * @param bean
	 * @param beanName
	 * @return
	 * @throws BeansException
	 */
	Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException;
}

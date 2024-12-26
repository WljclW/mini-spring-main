package org.springframework.beans.factory.config;

import org.springframework.beans.BeansException;

/**
 * 一句话：用于修改实例化后的bean的修改扩展点，用于在 Bean 实例化之后但在初始化 之前 和 之后 对Bean进行自定义处理。。
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
	 *	其作用是在Spring框架中 Bean 初始化之前进行处理。具体来说：
	 * 时机：该方法在Bean实例化完成、属性填充完毕后，但在调用初始化方法（如@PostConstruct注解的方法或InitializingBean接口的afterPropertiesSet方法）之前调用。
	 * 功能：允许开发者在这个阶段对即将初始化的Bean对象进行自定义处理或修改。例如，可以用于：
	 * 		修改Bean的状态或属性; 添加额外的初始化逻辑; 决定是否继续进行后续的初始化操作;
	 * 如果返回 null 或者未实现该方法，则继续正常的初始化流程；否则，返回修改后的Bean对象，该对象将用于后续的初始化和使用。
	 */
	Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException;

	/**
	 * 在bean执行初始化方法之后执行此方法
	 时机：该方法在Bean的所有初始化操作（包括@PostConstruct注解的方法、InitializingBean接口的afterPropertiesSet方法等）完成后调用。
	 功能：允许开发者在这个阶段对已经初始化完毕的Bean对象进行自定义处理或修改。例如，可以用于：
	 		添加额外的功能或代理、修改Bean的状态或属性、执行一些后续的验证或日志记录。
	 如果返回 null 或者未实现该方法，则继续正常的流程；否则，返回修改后的Bean对象，该对象将用于后续的操作。
	 */
	Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException;
}

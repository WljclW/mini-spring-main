package org.springframework.context.support;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 在 refresh 方法中创建 ApplicationContextAwareProcessor 的原因主要与 Spring 框架的依赖注入机制和上下文感知接口的实现有关。以下是具体的原因：
 * 处理上下文感知 Bean：
 * 		ApplicationContextAwareProcessor 负责处理实现了 ApplicationContextAware 接口的 Bean。这些 Bean 需要在初始化时被注
 * 		入 ApplicationContext 实例，以便它们可以在运行时访问应用上下文。
 * 自动装配上下文：
 * 		在 Spring 容器启动过程中，ApplicationContextAwareProcessor 确保所有标记为 ApplicationContextAware 的 Bean 能够自动
 * 		获得对 ApplicationContext 的引用。这使得这些 Bean 可以方便地访问上下文中的其他 Bean 或者使用上下文提供的功能。
 * 确保一致性：
 * 		将 ApplicationContextAwareProcessor 的创建放在 refresh 方法中可以确保每次容器刷新时，所有上下文感知 Bean 都能正确地获取
 * 		到最新的 ApplicationContext 实例，从而保证了上下文的一致性和可靠性。
 * 生命周期管理：
 * 		ApplicationContextAwareProcessor 是 Spring 容器生命周期的一部分，它确保了在 Bean 初始化阶段正确处理上下文感知逻辑，避免了
 * 		潜在的顺序问题或依赖冲突
 * */
public class ApplicationContextAwareProcessor implements BeanPostProcessor {

	private final ApplicationContext applicationContext;

	public ApplicationContextAwareProcessor(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof ApplicationContextAware) {	//如果是ApplicationContextAware接口类型，则调用setApplicationContext方法
			((ApplicationContextAware) bean).setApplicationContext(applicationContext);
		}
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return bean;		//直接把原始的bean传回去
	}
}

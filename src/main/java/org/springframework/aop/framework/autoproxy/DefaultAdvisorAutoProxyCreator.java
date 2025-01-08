package org.springframework.aop.framework.autoproxy;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.aopalliance.aop.Advice;

import org.springframework.aop.Advisor;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.Pointcut;
import org.springframework.aop.TargetSource;
import org.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

/**
 *
 */
public class DefaultAdvisorAutoProxyCreator implements InstantiationAwareBeanPostProcessor, BeanFactoryAware {

	private DefaultListableBeanFactory beanFactory;

	private Set<Object> earlyProxyReferences = new HashSet<>();

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (!earlyProxyReferences.contains(beanName)) {
			return wrapIfNecessary(bean, beanName);
		}

		return bean;
	}

	@Override
	public Object getEarlyBeanReference(Object bean, String beanName) throws BeansException {
		earlyProxyReferences.add(beanName);
		return wrapIfNecessary(bean, beanName);
	}

	/**
	 * ### 代码功能解释
	 *
	 * 这段代码的功能是为指定的bean创建代理对象（如果需要）。具体步骤如下：
	 *
	 * 1. **避免死循环**：如果当前bean是基础设施类（如Advice、Pointcut、Advisor），直接返回原bean。
	 * 2. **获取所有AspectJ表达式切面顾问**：从`beanFactory`中获取所有类型为`AspectJExpressionPointcutAdvisor`的bean。
	 * 3. **创建代理工厂**：初始化一个`ProxyFactory`实例。
	 * 4. **遍历顾问**：对于每个顾问，检查其切点是否匹配当前bean的类。如果匹配，则设置目标源和方法匹配器，并添加顾问到代理工厂。
	 * 5. **生成代理对象**：如果代理工厂中有任何顾问，则创建并返回代理对象；否则返回原bean。
	 * 6. **异常处理**：捕获并抛出异常。
	 * */
	protected Object wrapIfNecessary(Object bean, String beanName) {
		//避免死循环
		if (isInfrastructureClass(bean.getClass())) {
			return bean;
		}

		Collection<AspectJExpressionPointcutAdvisor> advisors = beanFactory.getBeansOfType(AspectJExpressionPointcutAdvisor.class)
				.values();
		try {
			ProxyFactory proxyFactory = new ProxyFactory();
			for (AspectJExpressionPointcutAdvisor advisor : advisors) {
				ClassFilter classFilter = advisor.getPointcut().getClassFilter();
				if (classFilter.matches(bean.getClass())) {
					TargetSource targetSource = new TargetSource(bean);
					proxyFactory.setTargetSource(targetSource);
					proxyFactory.addAdvisor(advisor);
					proxyFactory.setMethodMatcher(advisor.getPointcut().getMethodMatcher());
				}
			}
			if (!proxyFactory.getAdvisors().isEmpty()) {
				return proxyFactory.getProxy();
			}
		} catch (Exception ex) {
			throw new BeansException("Error create proxy bean for: " + beanName, ex);
		}
		return bean;
	}

	private boolean isInfrastructureClass(Class<?> beanClass) {
		return Advice.class.isAssignableFrom(beanClass)
				|| Pointcut.class.isAssignableFrom(beanClass)
				|| Advisor.class.isAssignableFrom(beanClass);
	}

	@Override	//BeanFactoryAware接口的抽象方法，用于让当前的实现类感知到所属的BeanFactory，即在 Spring 容器中获取对 BeanFactory 的引用
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = (DefaultListableBeanFactory) beanFactory;
	}

	@Override
	public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
		return null;
	}

	@Override
	public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
		return true;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public PropertyValues postProcessPropertyValues(PropertyValues pvs, Object bean, String beanName) throws BeansException {
		return pvs;
	}
}
/*
InstantiationAwareBeanPostProcessor接口的抽象方法：postProcessPropertyValues

**/

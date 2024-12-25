package org.springframework.beans.factory.support;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/*
管理Bean定义：维护一个 beanDefinitionMap，用于注册、存储、查找、删除指定的BeanDefinition
支持配置和扩展：实现了 ConfigurableListableBeanFactory 接口，允许对 Bean 工厂进行配置和定制;通过实现 BeanDefinitionRegistry 接口，支持动态注册新的 Bean 定义
* */
public class DefaultListableBeanFactory extends AbstractAutowireCapableBeanFactory
		implements ConfigurableListableBeanFactory, BeanDefinitionRegistry {

	private Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(256);


	@Override
	public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
		beanDefinitionMap.put(beanName, beanDefinition);
	}

	@Override
	public BeanDefinition getBeanDefinition(String beanName) throws BeansException {
		BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
		if (beanDefinition == null) {
			throw new BeansException("No bean named '" + beanName + "' is defined");
		}

		return beanDefinition;
	}

	@Override
	public boolean containsBeanDefinition(String beanName) {
		return beanDefinitionMap.containsKey(beanName);
	}

	//这个方法不错，是根据传进来的参数，来从BeanDefinitionMap中查找所有符合BeanDifinition，存储到Map中并返回。。泛型的典型使用可以参考这个方法
	@Override
	public <T> Map<String, T> getBeansOfType(Class<T> type) throws BeansException {
		Map<String, T> result = new HashMap<>();
		beanDefinitionMap.forEach((beanName, beanDefinition) -> {
			Class beanClass = beanDefinition.getBeanClass();
			//使用isAssignableFrom方法合理判断是不是type(指的是传进来的BeanPostProcessor接口)是不是每一个beanClass的类型 或者 超类型
			if (type.isAssignableFrom(beanClass)) {
				T bean = (T) getBean(beanName);
				result.put(beanName, bean);
			}
		});
		return result;
	}

	public <T> T getBean(Class<T> requiredType) throws BeansException {
		List<String> beanNames = new ArrayList<>();
		for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
			Class beanClass = entry.getValue().getBeanClass();
			if (requiredType.isAssignableFrom(beanClass)) {
				beanNames.add(entry.getKey());
			}
		}
		if (beanNames.size() == 1) {
			return getBean(beanNames.get(0), requiredType);
		}

		throw new BeansException(requiredType + "expected single bean but found " +
				beanNames.size() + ": " + beanNames);
	}

	@Override
	public String[] getBeanDefinitionNames() {
		Set<String> beanNames = beanDefinitionMap.keySet();
		return beanNames.toArray(new String[beanNames.size()]);
	}

	@Override
	public void preInstantiateSingletons() throws BeansException {
		beanDefinitionMap.forEach((beanName, beanDefinition) -> {
			//只有当bean是单例且不为懒加载才会被创建
			if (beanDefinition.isSingleton() && !beanDefinition.isLazyInit()) {
				getBean(beanName);
			}
		});
	}
}

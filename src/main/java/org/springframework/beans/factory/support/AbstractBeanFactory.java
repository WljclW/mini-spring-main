package org.springframework.beans.factory.support;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.util.StringValueResolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 这个类的这几个字段还是挺重要的，在refresh方法中会经常使用到
 *
 *
 */
public abstract class AbstractBeanFactory extends DefaultSingletonBeanRegistry implements ConfigurableBeanFactory {

	private final List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();	//添加所有的后置处理器，refresh方法的registerBeanPostProcessors步骤就是进行这个操作

	private final Map<String, Object> factoryBeanObjectCache = new HashMap<>();		//FactoryBean#getObject创建的bean对象的缓存。键是beanName，值是FactoryBean实现类的对象。

	private final List<StringValueResolver> embeddedValueResolvers = new ArrayList<StringValueResolver>();	//

	private ConversionService conversionService;


	/** 对于普通的bean，到执行这个方法的时候还没有完成创建。。但是BeanPostProcessor这种类型的bean就创建完成了
	 * 首先去三级缓存获取bean，获取到的话会经过getObjectForBeanInstance方法(这个方法主要的作用就是判断 是不是FactoryBean类型，是的话会
	 * 		通过缓存去获取 或者 通过getObject方法来后来拿到返回的bean)。
	 * */
	@Override
	public Object getBean(String name) throws BeansException {
		Object sharedInstance = getSingleton(name);		//通过三级缓存去查找bean。如果是普通的类型，在这里初次得到的是null,而对于BeanFactoryPostProcessor、BeanPostProcessor之前的时候就创建完了，从三级缓存能够查询到
		if (sharedInstance != null) {	//含义：如果三级缓存能找到，就要看是不是FactoryBean类型
			//如果是FactoryBean，从FactoryBean#getObject中创建 或者 从缓存中获取bean。。但是对于普通对象，此时必然是null，普通对象这个时候还不在三级缓存呢
			return getObjectForBeanInstance(sharedInstance, name);
		}
		//下面就是按照BeanDefinition来创建指定的bean对象
		BeanDefinition beanDefinition = getBeanDefinition(name);	//去map中拿到name对应的BeanDefinition
		Object bean = createBean(name, beanDefinition);		//利用beanName和beanDefinition信息创建bean对象
		return getObjectForBeanInstance(bean, name);
	}

	/**
	 * 如果是FactoryBean，从FactoryBean#getObject中创建bean
	 */
	protected Object getObjectForBeanInstance(Object beanInstance, String beanName) {
		Object object = beanInstance;
		if (beanInstance instanceof FactoryBean) {		//这一步骤的作用是什么
			FactoryBean factoryBean = (FactoryBean) beanInstance;
			try {
				if (factoryBean.isSingleton()) {
					//singleton作用域bean，从缓存中获取
					object = this.factoryBeanObjectCache.get(beanName);
					if (object == null) {
						object = factoryBean.getObject();
						this.factoryBeanObjectCache.put(beanName, object);
					}
				} else {
					//prototype作用域bean，新创建bean
					object = factoryBean.getObject();
				}
			} catch (Exception ex) {
				throw new BeansException("FactoryBean threw exception on object[" + beanName + "] creation", ex);
			}
		}

		return object;
	}

	@Override
	public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
		return ((T) getBean(name));
	}

	@Override
	public boolean containsBean(String name) {
		return containsBeanDefinition(name);
	}

	protected abstract boolean containsBeanDefinition(String beanName);

	protected abstract Object createBean(String beanName, BeanDefinition beanDefinition) throws BeansException;

	protected abstract BeanDefinition getBeanDefinition(String beanName) throws BeansException;

	@Override
	public void addBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
		//有则覆盖
		this.beanPostProcessors.remove(beanPostProcessor);
		this.beanPostProcessors.add(beanPostProcessor);
	}

	public List<BeanPostProcessor> getBeanPostProcessors() {
		return this.beanPostProcessors;
	}

	public void addEmbeddedValueResolver(StringValueResolver valueResolver) {
		this.embeddedValueResolvers.add(valueResolver);
	}

	public String resolveEmbeddedValue(String value) {
		String result = value;
		for (StringValueResolver resolver : this.embeddedValueResolvers) {
			result = resolver.resolveStringValue(result);
		}
		return result;
	}

	@Override
	public ConversionService getConversionService() {
		return conversionService;
	}

	@Override
	public void setConversionService(ConversionService conversionService) {
		this.conversionService = conversionService;
	}
}

/**
 * embeddedValueResolvers字段解释：
 * 【总结】用于存储一系列值解析器（StringValueResolver），这些解析器用于解析 Bean 定义中的占位符（如 ${property.key}）和其他嵌入式表达式。通
 * 		过这些解析器，Spring 可以将占位符替换为实际的属性值或计算结果。
 * 【主要作用】
 * 		占位符解析：
 * 			解析配置文件或注解中的占位符（如 ${property.key}），并将其替换为实际的属性值。
 * 		表达式解析：
 * 			解析 SpEL（Spring Expression Language）表达式或其他类型的表达式，并计算其结果。
 * 		多级解析：
 * 			支持多个解析器按顺序执行，允许更复杂的解析逻辑，例如先从环境变量中查找，再从属性文件中查找。
 * 		扩展性：
 * 			允许开发者自定义和添加新的解析器，以满足特定需求。
 * 【内置的StringValueResolver的实现类】
 *		PropertyPlaceholderHelper：用于解析占位符（如 ${property.key}）
 * 		SpelExpressionEvaluator：用于解析SpEL表达式。
 * */
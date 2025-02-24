package org.springframework.beans.factory.support;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.TypeUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.*;
import org.springframework.core.convert.ConversionService;

import java.lang.reflect.Method;


public abstract class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory
		implements AutowireCapableBeanFactory {

	private InstantiationStrategy instantiationStrategy = new SimpleInstantiationStrategy();

	@Override
	protected Object createBean(String beanName, BeanDefinition beanDefinition) throws BeansException {
		//如果bean需要代理，则直接返回代理对象....resolveBeforeInstantiation方法主要是进行bean初始化之前和之后的工作；如果bean不需要代理就利用doCreateBean创建bean
		Object bean = resolveBeforeInstantiation(beanName, beanDefinition);	//实例化之前处理bean。
		if (bean != null) {		//如果这里拿到的不是null，就直接返回。不会进入到doCreateBean这个普通的创建过程
			return bean;
		}

		return doCreateBean(beanName, beanDefinition);		//如果需要代理会到这。这一步就仅仅是根据BeanDefinition创建一个单纯的bean对象
	}

	/**
	 * 执行InstantiationAwareBeanPostProcessor的方法，如果bean需要代理，直接返回代理对象
	 *
	 * @param beanName
	 * @param beanDefinition
	 * @return
	 */
	protected Object resolveBeforeInstantiation(String beanName, BeanDefinition beanDefinition) {	//实例化之前进行的处理
		Object bean = applyBeanPostProcessorsBeforeInstantiation(beanDefinition.getBeanClass(), beanName);	//beanDefinition.getBeanClass()是从java文件夹开始向下寻找的，全路径是相对于java文件夹来说的
		if (bean != null) {
			bean = applyBeanPostProcessorsAfterInitialization(bean, beanName);
		}
		return bean;
	}

	protected Object applyBeanPostProcessorsBeforeInstantiation(Class beanClass, String beanName) {
		for (BeanPostProcessor beanPostProcessor : getBeanPostProcessors()) {
			if (beanPostProcessor instanceof InstantiationAwareBeanPostProcessor) {
				Object result = ((InstantiationAwareBeanPostProcessor) beanPostProcessor).postProcessBeforeInstantiation(beanClass, beanName);
				if (result != null) {
					return result;
				}
			}
		}

		return null;
	}

	protected Object doCreateBean(String beanName, BeanDefinition beanDefinition) {
		Object bean;
		try {
			bean = createBeanInstance(beanDefinition);		//通过无参构造器创建对象(准确的说是调用实例化策略类的实例化方法，实现自定义的实例化过程)。此时bean的各个属性值不是null 就是 零值

			//为解决循环依赖问题，将实例化后的bean放进缓存中提前暴露
			if (beanDefinition.isSingleton()) {
				Object finalBean = bean;
				addSingletonFactory(beanName, new ObjectFactory<Object>() {		//创建bean之后放入三级缓存，此时bean的属性值可能还没有设置，但是可以获取到。
					@Override
					public Object getObject() throws BeansException {	//ObjectFactory接口，函数式接口。
						return getEarlyBeanReference(beanName, beanDefinition, finalBean);
					}
				});
			}

			//实例化bean之后执行。
			boolean continueWithPropertyPopulation = applyBeanPostProcessorsAfterInstantiation(beanName, bean);	//bean实例化之后，初始化之前处理bean
			if (!continueWithPropertyPopulation) {		//如果上一步经过BeanPostProcessor的处理(返回true)，这里就直接返回bean
				return bean;
			}
			//在设置bean属性之前，允许BeanPostProcessor修改属性值(是直接修改beanDefinition中的，即beanDefinition.getPropertyValues())。这一步就会进行Autowired注解 以及 Value注解的属性填充。
			applyBeanPostProcessorsBeforeApplyingPropertyValues(beanName, bean, beanDefinition);
			//为bean填充属性，这一步是利用BeanDefinition中的PropertyValues为"实例化"后的bean填充属性值
			applyPropertyValues(beanName, bean, beanDefinition);
			//按顺序执行：BeanPostProcessor接口实现类的postProcessBeforeInitialization、执行bean的初始化方法、BeanPostProcessor接口实现类的postProcessAfterInitialization
			bean = initializeBean(beanName, bean, beanDefinition);
		} catch (Exception e) {
			throw new BeansException("Instantiation of bean failed", e);
		}

		//注册 定义销毁方法的bean
		registerDisposableBeanIfNecessary(beanName, bean, beanDefinition);

 		Object exposedObject = bean;
		if (beanDefinition.isSingleton()) {
			//如果有代理对象，此处获取代理对象
			exposedObject = getSingleton(beanName);
			addSingleton(beanName, exposedObject);
		}
		return exposedObject;
	}

	/**
	 作用是在Spring框架中获取Bean的早期引用（即在Bean完全初始化之前获取对它的引用）。具体来说：
	 时机：该方法在Bean实例化之后、属性填充和初始化之前调用。
	 功能：允许开发者在Bean尚未完全初始化时提前获取对该Bean的引用。这主要用于解决循环依赖问题，确保即使在Bean未完全初始化的情况下，也可以被其他Bean引用。
	 主要用途
	 解决循环依赖：当两个或多个Bean之间存在相互依赖时，Spring可以通过返回早期引用（通常是代理对象）来避免循环依赖导致的无限递归问题。
	 提供灵活性：允许在Bean生命周期的不同阶段对其进行访问和处理。
	 */
	protected Object getEarlyBeanReference(String beanName, BeanDefinition beanDefinition, Object bean) {
		Object exposedObject = bean;
		for (BeanPostProcessor bp : getBeanPostProcessors()) {
			if (bp instanceof InstantiationAwareBeanPostProcessor) {
				exposedObject = ((InstantiationAwareBeanPostProcessor) bp).getEarlyBeanReference(exposedObject, beanName);
				if (exposedObject == null) {
					return exposedObject;
				}
			}
		}

		return exposedObject;
	}

	/**
	 * bean实例化后执行，如果返回false，不执行后续设置属性的逻辑
	 *
	 * @param beanName
	 * @param bean
	 * @return
	 */
	private boolean applyBeanPostProcessorsAfterInstantiation(String beanName, Object bean) {
		boolean continueWithPropertyPopulation = true;
		for (BeanPostProcessor beanPostProcessor : getBeanPostProcessors()) {	//又是看一下beanPostProcessors是不是有InstantiationAwareBeanPostProcessor类型，然后操作一下创建的bean
			if (beanPostProcessor instanceof InstantiationAwareBeanPostProcessor) {
				if (!((InstantiationAwareBeanPostProcessor) beanPostProcessor).postProcessAfterInstantiation(bean, beanName)) {
					continueWithPropertyPopulation = false;
					break;
				}
			}
		}
		return continueWithPropertyPopulation;
	}

	/**
	 * 在设置bean属性之前，允许BeanPostProcessor修改属性值
	 *
	 * @param beanName
	 * @param bean
	 * @param beanDefinition
	 */
	protected void applyBeanPostProcessorsBeforeApplyingPropertyValues(String beanName, Object bean, BeanDefinition beanDefinition) {
		for (BeanPostProcessor beanPostProcessor : getBeanPostProcessors()) {
			if (beanPostProcessor instanceof InstantiationAwareBeanPostProcessor) {
				PropertyValues pvs = ((InstantiationAwareBeanPostProcessor) beanPostProcessor).postProcessPropertyValues(beanDefinition.getPropertyValues(), bean, beanName);	//占位符的替换就是在这里进行
				if (pvs != null) {
					for (PropertyValue propertyValue : pvs.getPropertyValues()) {
						beanDefinition.getPropertyValues().addPropertyValue(propertyValue);
					}
				}
			}
		}
	}

	/**
	 * 注册有销毁方法的bean，即bean继承自DisposableBean或有自定义的销毁方法
	 *
	 * @param beanName
	 * @param bean
	 * @param beanDefinition
	 */
	protected void registerDisposableBeanIfNecessary(String beanName, Object bean, BeanDefinition beanDefinition) {
		//只有singleton类型bean会执行销毁方法
		if (beanDefinition.isSingleton()) {
			if (bean instanceof DisposableBean || StrUtil.isNotEmpty(beanDefinition.getDestroyMethodName())) {		//判断该bean是不是实现了DisposableBean接口的bean(这个接口唯一的抽象方法就是设置销毁方法) 或者 beanDefinition中是不是制定了销毁方法
				registerDisposableBean(beanName, new DisposableBeanAdapter(bean, beanName, beanDefinition));
			}
		}
	}

	/**
	 * 实例化bean
	 *
	 * @param beanDefinition
	 * @return
	 */
	protected Object createBeanInstance(BeanDefinition beanDefinition) {
		return getInstantiationStrategy().instantiate(beanDefinition);	//先拿到实例化策略；然后执行instantiate方法实例化对象
	}

	/**
	 * 为bean填充属性。这里就是 利用BeanDefinition中定义的属性(PropertyValue)来为创建的bean填充。注意前一步是有修改机会的，见
	 * 		InstantiationAwareBeanPostProcessor接口
	 *
//	 * @param bean
//	 * @param beanDefinition
	 */
	protected void applyPropertyValues(String beanName, Object bean, BeanDefinition beanDefinition) {
		try {
			for (PropertyValue propertyValue : beanDefinition.getPropertyValues().getPropertyValues()) {
				String name = propertyValue.getName();
				Object value = propertyValue.getValue();
				if (value instanceof BeanReference) {
					// beanA依赖beanB。看beanB是不是在三级缓存，如果不是，先实例化beanB
					BeanReference beanReference = (BeanReference) value;	//由于if判断满足，因此这里转换没有问题
					value = getBean(beanReference.getBeanName());	//对于BeanReference类型的属性需要通过getBean进行获取
				} else {
					//类型转换
					Class<?> sourceType = value.getClass();
					Class<?> targetType = (Class<?>) TypeUtil.getFieldType(bean.getClass(), name);
					ConversionService conversionService = getConversionService();
					if (conversionService != null) {
						if (conversionService.canConvert(sourceType, targetType)) {
							value = conversionService.convert(value, targetType);
						}
					}
				}

				//通过反射设置属性
				BeanUtil.setFieldValue(bean, name, value); //bean是当前的bean实例，name是属性的名称，value是属性值
			}
		} catch (Exception ex) {
			throw new BeansException("Error setting property values for bean: " + beanName, ex);
		}
	}

	protected Object initializeBean(String beanName, Object bean, BeanDefinition beanDefinition) {
		if (bean instanceof BeanFactoryAware) {
			((BeanFactoryAware) bean).setBeanFactory(this);
		}

		//执行BeanPostProcessor的前置处理方法即BeforeInitialization方法（Bean“初始化前”的处理，这里的"初始化前"指的是执行初始化方法之前）
		Object wrappedBean = applyBeanPostProcessorsBeforeInitialization(bean, beanName);

		try {
			invokeInitMethods(beanName, wrappedBean, beanDefinition);	//执行bean的初始化方法(可以通过实现接口，也可以是bean标签设置————此法等价于方法加注解)
		} catch (Throwable ex) {
			throw new BeansException("Invocation of init method of bean[" + beanName + "] failed", ex);
		}

		//执行BeanPostProcessor的后置处理（Bean”初始化“的后置处理）
		wrappedBean = applyBeanPostProcessorsAfterInitialization(wrappedBean, beanName);
		return wrappedBean;
	}

	@Override
	public Object applyBeanPostProcessorsBeforeInitialization(Object existingBean, String beanName)
			throws BeansException {
		Object result = existingBean;
		for (BeanPostProcessor processor : getBeanPostProcessors()) {
			Object current = processor.postProcessBeforeInitialization(result, beanName);	//调用BeanPostProcessor前置处理器()
			if (current == null) {
				return result;
			}
			result = current;
		}
		return result;
	}

	@Override
	public Object applyBeanPostProcessorsAfterInitialization(Object existingBean, String beanName)
			throws BeansException {

		Object result = existingBean;
		for (BeanPostProcessor processor : getBeanPostProcessors()) {
			Object current = processor.postProcessAfterInitialization(result, beanName);
			if (current == null) {
				return result;
			}
			result = current;
		}
		return result;
	}

	/**
	 * 执行bean的初始化方法
	 *
	 * @param beanName
	 * @param bean
	 * @param beanDefinition
	 * @throws Throwable
	 */
	protected void invokeInitMethods(String beanName, Object bean, BeanDefinition beanDefinition) throws Throwable {
		if (bean instanceof InitializingBean) {
			((InitializingBean) bean).afterPropertiesSet();
		}
		String initMethodName = beanDefinition.getInitMethodName();
		if (StrUtil.isNotEmpty(initMethodName) && !(bean instanceof InitializingBean && "afterPropertiesSet".equals(initMethodName))) {
			Method initMethod = ClassUtil.getPublicMethod(beanDefinition.getBeanClass(), initMethodName);
			if (initMethod == null) {
				throw new BeansException("Could not find an init method named '" + initMethodName + "' on bean with name '" + beanName + "'");
			}
			initMethod.invoke(bean);	//通过反射调用初始化方法
		}
	}

	public InstantiationStrategy getInstantiationStrategy() {
		return instantiationStrategy;
	}

	public void setInstantiationStrategy(InstantiationStrategy instantiationStrategy) {
		this.instantiationStrategy = instantiationStrategy;
	}
}

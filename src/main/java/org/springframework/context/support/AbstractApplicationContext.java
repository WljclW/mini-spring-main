package org.springframework.context.support;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.io.DefaultResourceLoader;

import java.util.Collection;
import java.util.Map;

/**
 * 抽象应用上下文
 *
 */
public abstract class AbstractApplicationContext extends DefaultResourceLoader implements ConfigurableApplicationContext {

	public static final String APPLICATION_EVENT_MULTICASTER_BEAN_NAME = "applicationEventMulticaster";

	public static final String CONVERSION_SERVICE_BEAN_NAME = "conversionService";

	private ApplicationEventMulticaster applicationEventMulticaster;

	// BeanPostProcessor(允许在bean实例化之后 但是 在初始化之前 做一些处理)
	// BeanFactoryPostProcessor(仅一个抽象方法，用于修改bean的配置元数据。BeanDefinition创建完成(即refreshBeanFactory()方法)之后 但是 在bean实例化之前)
	@Override
	public void refresh() throws BeansException {
		//创建BeanFactory，根据xml文件创建所有的BeanDefinition并且存放在beanDefinitionMap。会根据所有bean的定义信息创建对应的beanDefinition对象存起来
		refreshBeanFactory();	//最终会执型doLoadBeanDefinitions方法，会利用流来读取并解析xml文件将所有的bean标签定义的bean，每一个bean标签构造出一个BeanDefinition，并将所有的BeanDefinition存储在BeanDefinitionMap中
		ConfigurableListableBeanFactory beanFactory = getBeanFactory();		//默认是DefaultListableBeanFactory对象

		//添加ApplicationContextAwareProcessor，让继承自ApplicationContextAware的bean能感知bean。。由于这里的方式是直接new的，因此并不在容器中管理
		beanFactory.addBeanPostProcessor(new ApplicationContextAwareProcessor(this));	//ApplicationContextAwareProcessor内部就是持有一个ApplicationContext类型的对象

		/**
		 * ApplicationContextAwareProcessor 的存在和使用主要是为了处理那些实现了 ApplicationContextAware 接口的 bean。这些 bean
		 * 需要获得 ApplicationContext 的引用，以便它们可以访问 Spring 容器中的其他 bean 和资源。
		 * */
		//在bean实例化之前，执行BeanFactoryPostProcessor（这些类主要用于实现对于beanDefinition的修改或操作），出现在所有Bean的实例化(创建)之前。
		invokeBeanFactoryPostProcessors(beanFactory);	//主要涉及到BeanDifinitionMap中的BeanFactoryPostProcessor接口类的处理

		//BeanPostProcessor需要提前与其他bean实例化之前注册。。在这之前其实已经注册过一个了，就是"new ApplicationContextAwareProcessor(this)"为了实现后续的感知接口的功能
		registerBeanPostProcessors(beanFactory);	//注意区分上一步中的BeanFactoryPostProcessor（用于实现对于bean定义信息的修改）；实现BeanPostProcessor接口的bean在这里就会进行三级缓存的添加

		//初始化事件发布者。。。实际上就是初始化一个SimpleApplicationEventMulticaster对象，并 添加到一级缓存
		initApplicationEventMulticaster();

		//注册事件监听器。。。会通过getBeansOfType方法查找ApplicationListener.class接口类型的所有bean,并将这些bean添加到ApplicationEventMulticaster类的set属性applicationListeners中
		//因此可以通过实现ApplicationListener接口的bean，当容器发布事件时，会调用这个bean的onApplicationEvent方法
		registerListeners();

		//注册类型转换器 和 提前实例化单例bean
		finishBeanFactoryInitialization(beanFactory);

		//发布容器刷新完成事件
		finishRefresh();
	}

	protected void finishBeanFactoryInitialization(ConfigurableListableBeanFactory beanFactory) {
		//设置类型转换器，作用？？
		if (beanFactory.containsBean(CONVERSION_SERVICE_BEAN_NAME)) {
			Object conversionService = beanFactory.getBean(CONVERSION_SERVICE_BEAN_NAME);
			if (conversionService instanceof ConversionService) {
				beanFactory.setConversionService((ConversionService) conversionService);
			}
		}

		//提前实例化单例bean。。普通的、单例的bean就会在这一步进行创建完成(BeanPostProcessor接口实现类的bean在之前添加时通过getBean方法已经完成创建了)。同时涉及到了三级缓存的使用。。。默认bean都是单例的
		beanFactory.preInstantiateSingletons();
	}

	/**
	 * 创建BeanFactory，并加载BeanDefinition
	 *
	 * @throws BeansException
	 */
	protected abstract void refreshBeanFactory() throws BeansException;

	/**
	 * 在bean实例化之前，执行BeanFactoryPostProcessor
	 * 主要的操作就是通过beanFactory.getBeansOfType(BeanFactoryPostProcessor.class)在beanDefinition的map中查找所有实现了BeanFactoryPostProcessor
	 * 		这个接口的类，并将“beanName，bean实例”这样的映射放入到下面方法的beanFactoryPostProcessorMap中(注意：此时map中的值是bean实例不是beanDefinition)
	 * 		然后利用for循环，一次执行其中所有实例的postProcessBeanFactory方法，这个方法是接口BeanFactoryPostProcessor中唯一的抽象方法
	 *
	 * @param beanFactory
	 */
	protected void invokeBeanFactoryPostProcessors(ConfigurableListableBeanFactory beanFactory) {
		Map<String, BeanFactoryPostProcessor> beanFactoryPostProcessorMap = beanFactory.getBeansOfType(BeanFactoryPostProcessor.class);		//获取BeanFactoryPostProcessor类型的所有bean的定义信息(是处理BeanDefinition的扩展点)
		for (BeanFactoryPostProcessor beanFactoryPostProcessor : beanFactoryPostProcessorMap.values()) {
			beanFactoryPostProcessor.postProcessBeanFactory(beanFactory);
		}
	}

	/**
	 * 注册BeanPostProcessor。。。这一步之后ioc容器中所有的BeanPostProcessor也就记录完成了，存储于AbstractBeanFactory#beanPostProcessors
	 * 这里的beanFactory参数传入的也是DefaultListableBeanFactory，可见这个类也是ConfigurableListableBeanFactory，回头可以看一下这
	 * 		个类的继承关系图
	 *
	 * @param beanFactory
	 */
	protected void registerBeanPostProcessors(ConfigurableListableBeanFactory beanFactory) {
		Map<String, BeanPostProcessor> beanPostProcessorMap = beanFactory.getBeansOfType(BeanPostProcessor.class);	//从BeanDefinitionMap中拿
		for (BeanPostProcessor beanPostProcessor : beanPostProcessorMap.values()) {
			beanFactory.addBeanPostProcessor(beanPostProcessor);
		}
	}

	/**
	 * 初始化事件发布者，下面的只是一种简单的实现。源码中实际上这里的逻辑是：
	 * 情况一：如果容器里面有名为applicationEventMulticaster的bean，则将该bean设为上下文中的事件广播器。
	 * 情况二：如果容器里面没有applicationEventMulticaster的bean，默认创建SimpleApplicationEventMulticaster来代替。
	 */
	protected void initApplicationEventMulticaster() {
		ConfigurableListableBeanFactory beanFactory = getBeanFactory();
		applicationEventMulticaster = new SimpleApplicationEventMulticaster(beanFactory);
		beanFactory.addSingleton(APPLICATION_EVENT_MULTICASTER_BEAN_NAME, applicationEventMulticaster);		//将创建的对象直接注册在一级缓存。由于这里是创建对象直接存到一级缓存，因此可知：BeanDefinitionMap中并不是包括了所有的bean，我们可以通过new的方式创建并放入到三级缓存
	}

	/**
	 * 注册事件监听器
	 */
	protected void registerListeners() {	//获取所有ApplicationListener接口类型的bean，并添加到applicationEventMulticaster中
		Collection<ApplicationListener> applicationListeners = getBeansOfType(ApplicationListener.class).values();
		for (ApplicationListener applicationListener : applicationListeners) {
			applicationEventMulticaster.addApplicationListener(applicationListener);
		}
	}

	/**
	 * 发布容器刷新完成事件
	 */
	protected void finishRefresh() {
		publishEvent(new ContextRefreshedEvent(this));
	}

	@Override
	public void publishEvent(ApplicationEvent event) {
		applicationEventMulticaster.multicastEvent(event);
	}

	@Override
	public boolean containsBean(String name) {
		return getBeanFactory().containsBean(name);
	}

	@Override
	public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
		return getBeanFactory().getBean(name, requiredType);
	}

	@Override
	public <T> Map<String, T> getBeansOfType(Class<T> type) throws BeansException {
		return getBeanFactory().getBeansOfType(type);
	}

	public <T> T getBean(Class<T> requiredType) throws BeansException {
		return getBeanFactory().getBean(requiredType);
	}

	@Override
	public Object getBean(String name) throws BeansException {
		return getBeanFactory().getBean(name);
	}

	public String[] getBeanDefinitionNames() {
		return getBeanFactory().getBeanDefinitionNames();
	}

	public abstract ConfigurableListableBeanFactory getBeanFactory();

	public void close() {
		doClose();
	}

	public void registerShutdownHook() {
		Thread shutdownHook = new Thread() {
			public void run() {
				doClose();		//这个方法是实现优雅关闭的关键
			}
		};
		Runtime.getRuntime().addShutdownHook(shutdownHook);

	}

	protected void doClose() {
		//发布容器关闭事件
		publishEvent(new ContextClosedEvent(this));

		//执行单例bean的销毁方法
		destroyBeans();
	}

	protected void destroyBeans() {
		getBeanFactory().destroySingletons();
	}
}


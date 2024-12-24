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
 * @author derekyi
 * @date 2020/11/28
 */
public abstract class AbstractApplicationContext extends DefaultResourceLoader implements ConfigurableApplicationContext {

	public static final String APPLICATION_EVENT_MULTICASTER_BEAN_NAME = "applicationEventMulticaster";

	public static final String CONVERSION_SERVICE_BEAN_NAME = "conversionService";

	private ApplicationEventMulticaster applicationEventMulticaster;

	@Override
	public void refresh() throws BeansException {
		//创建BeanFactory，并加载BeanDefinition。会将所有bean的定义信息创建对应的beanDefinition对象存起来
		refreshBeanFactory();	//最终会执型doLoadBeanDefinitions方法，会利用流来读取并解析xml文件将所有的bean标签定义的bean，每一个bean标签对应一个BeanDefinition，所有的BeanDefinition存储在BeanDefinitionMap中
		ConfigurableListableBeanFactory beanFactory = getBeanFactory();		//默认是DefaultListableBeanFactory对象

		//添加ApplicationContextAwareProcessor，让继承自ApplicationContextAware的bean能感知bean
		beanFactory.addBeanPostProcessor(new ApplicationContextAwareProcessor(this));	//ApplicationContextAwareProcessor内部就是持有一个ApplicationContext类型的对象

		/**
		 * ApplicationContextAwareProcessor 的存在和使用主要是为了处理那些实现了 ApplicationContextAware 接口的 bean。这些 bean
		 * 需要获得 ApplicationContext 的引用，以便它们可以访问 Spring 容器中的其他 bean 和资源。
		 * */
		//在bean实例化之前，执行BeanFactoryPostProcessor
		invokeBeanFactoryPostProcessors(beanFactory);	//主要涉及到BeanDifinitionMap中的BeanFactoryPostProcessor接口类的处理

		//BeanPostProcessor需要提前与其他bean实例化之前注册
		registerBeanPostProcessors(beanFactory);

		//初始化事件发布者。。。实际上就是初始化一个SimpleApplicationEventMulticaster对象，并 添加到一级缓存
		initApplicationEventMulticaster();

		//注册事件监听器。。。会通过getBeansOfType方法查找ApplicationListener.class接口类型的所有bean,并将这些bean添加到ApplicationEventMulticaster类的set属性applicationListeners中
		registerListeners();

		//注册类型转换器和提前实例化单例bean
		finishBeanFactoryInitialization(beanFactory);

		//发布容器刷新完成事件
		finishRefresh();
	}

	protected void finishBeanFactoryInitialization(ConfigurableListableBeanFactory beanFactory) {
		//设置类型转换器
		if (beanFactory.containsBean(CONVERSION_SERVICE_BEAN_NAME)) {
			Object conversionService = beanFactory.getBean(CONVERSION_SERVICE_BEAN_NAME);
			if (conversionService instanceof ConversionService) {
				beanFactory.setConversionService((ConversionService) conversionService);
			}
		}

		//提前实例化单例bean。。单例的bean就会在这一步进行创建完成，同时涉及到了三级缓存的使用。。。默认bean都是单例的
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
		Map<String, BeanFactoryPostProcessor> beanFactoryPostProcessorMap = beanFactory.getBeansOfType(BeanFactoryPostProcessor.class);		//获取BeanFactoryPostProcessor类型的所有bean的定义信息
		for (BeanFactoryPostProcessor beanFactoryPostProcessor : beanFactoryPostProcessorMap.values()) {
			beanFactoryPostProcessor.postProcessBeanFactory(beanFactory);
		}
	}

	/**
	 * 注册BeanPostProcessor
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
	 * 初始化事件发布者
	 */
	protected void initApplicationEventMulticaster() {
		ConfigurableListableBeanFactory beanFactory = getBeanFactory();
		applicationEventMulticaster = new SimpleApplicationEventMulticaster(beanFactory);
		beanFactory.addSingleton(APPLICATION_EVENT_MULTICASTER_BEAN_NAME, applicationEventMulticaster);
	}

	/**
	 * 注册事件监听器
	 */
	protected void registerListeners() {
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
				doClose();
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


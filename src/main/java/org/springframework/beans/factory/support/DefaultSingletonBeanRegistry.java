package org.springframework.beans.factory.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.SingletonBeanRegistry;

/**
 * 负责单例Bean的生命周期管理，包括创建、缓存(三级缓存)、获取和销毁
 *
 * */
public class DefaultSingletonBeanRegistry implements SingletonBeanRegistry {

	 /**
	  * 一级缓存
	  */
	private Map<String, Object> singletonObjects = new HashMap<>();

	 /**
	  * 二级缓存
	  */
	private Map<String, Object> earlySingletonObjects = new HashMap<>();

	 /**
	  * 三级缓存
	  */
	private Map<String, ObjectFactory<?>> singletonFactories = new HashMap<String, ObjectFactory<?>>();

	private final Map<String, DisposableBean> disposableBeans = new HashMap<>();	//有销毁方法的bean，会注册到这里。string是bean的name

	@Override
	public Object getSingleton(String beanName) {		//三级缓存出现的地方。。允许在 Bean 尚未完全初始化之前就可以被其他 Bean 引用，从而解决循环依赖问题。
		Object singletonObject = singletonObjects.get(beanName);
		if (singletonObject == null) {	//一级缓存查出来是null
			singletonObject = earlySingletonObjects.get(beanName);
			if (singletonObject == null) {	//二级缓存也没有查到
				ObjectFactory<?> singletonFactory = singletonFactories.get(beanName);	//从三级缓存获取，得到的是一个ObjectFactory
				if (singletonFactory != null) {		//三级缓存查到了。查到的话就会通过getObject来获取
					singletonObject = singletonFactory.getObject();
					//从三级缓存放进二级缓存
					earlySingletonObjects.put(beanName, singletonObject);
					singletonFactories.remove(beanName);
				}
			}
		}
		return singletonObject;
	}

	@Override
	public void addSingleton(String beanName, Object singletonObject) {		//用于将一个已经创建好的 Bean 实例注册为单例 Bean 并存储在容器的单例缓存中
		singletonObjects.put(beanName, singletonObject); // 1
		earlySingletonObjects.remove(beanName); // 2
		singletonFactories.remove(beanName); // 3
	}

	protected void addSingletonFactory(String beanName, ObjectFactory<?> singletonFactory) {
		singletonFactories.put(beanName, singletonFactory);
	}

	public void registerDisposableBean(String beanName, DisposableBean bean) {
		disposableBeans.put(beanName, bean);
	}

	public void destroySingletons() {
		ArrayList<String> beanNames = new ArrayList<>(disposableBeans.keySet());	//从disposableBeans拿到有销毁方法的bean
		for (String beanName : beanNames) {
			DisposableBean disposableBean = disposableBeans.remove(beanName);
			try {
				disposableBean.destroy();
			} catch (Exception e) {
				throw new BeansException("Destroy method on bean with name '" + beanName + "' threw an exception", e);
			}
		}
	}
}

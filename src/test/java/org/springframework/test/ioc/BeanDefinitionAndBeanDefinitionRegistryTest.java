package org.springframework.test.ioc;

import org.junit.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.test.service.HelloService;

/**
 * 这段代码的逻辑(其实容器自动初始化，根据BeanDefinition来创建bean基本上就是同样的逻辑)：
 * 		1. 默认情况下都是创建DefaultListableBeanFactory类型的；
 * 		2. 通过自己创建BeanDefinition，然后注册到DefaultListableBeanFactory中可以实行按同样的逻辑创建bean
 * 		3. 然后利用getBean来尝试获取bean。
 */
public class BeanDefinitionAndBeanDefinitionRegistryTest {

	@Test
	public void testBeanFactory() throws Exception {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		BeanDefinition beanDefinition = new BeanDefinition(HelloService.class);
		beanFactory.registerBeanDefinition("helloService", beanDefinition);

		HelloService helloService = (HelloService) beanFactory.getBean("helloService");	//getBean的逻辑：先尝试去三级缓存获取，如果获取不到则根据BeanDefinition创建bean
		helloService.sayHello();
	}
}

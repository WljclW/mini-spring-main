package org.springframework.test.ioc;

import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.bean.A;
import org.springframework.test.bean.B;

import static org.assertj.core.api.Java6Assertions.assertThat;

/**
 *	作用：演示循环引用的时候，bean的实例化和初始化过程。这是一个很常规的流程，不涉及代理等复杂形式
 */
public class CircularReferenceWithoutProxyBeanTest {

	@Test
	public void testCircularReference() throws Exception {
		ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:circular-reference-without-proxy-bean.xml");
		A a = applicationContext.getBean("a", A.class);
		B b = applicationContext.getBean("b", B.class);
		assertThat(a.getB() == b).isTrue();
	}
}
/**
 * 1. 在执行initApplicationEventMulticaster方法的时候，可以看一下beanFactory的内容，此时可以看到：
 * 		beanDefinitionMap包含两个bean的定义信息，制作图纸；
 * 		beanPostProcessors包含一个ApplicationContextAwareProcessor，这个处理器实现了BeanPostProcessor接口，
 * 		singletonObjects包含一个applicationEventMulticaster实例，这个实例是在initApplicationEventMulticaster中创建并放入到一级缓存的
 * */
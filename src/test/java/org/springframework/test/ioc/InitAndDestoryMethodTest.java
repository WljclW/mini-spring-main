package org.springframework.test.ioc;

import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 作用：展示 初始化方法 和 销毁方法 的使用，以及 作用时机。
 * 		他们都有两种方法去实现————
 * 			初始化方法：通过实现 InitializingBean 接口和通过 @PostConstruct 注解(等价于在xml文件中的Bean标签内使用"init-method"指定)。
 * 			销毁方法：通过实现 DisposableBean 接口和通过 @PreDestroy 注解(等价于在xml文件中的Bean标签内使用"destroy-method"指定)。
 */
public class InitAndDestoryMethodTest {

	@Test
	public void testInitAndDestroyMethod() throws Exception {
		ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:init-and-destroy-method.xml");
		applicationContext.registerShutdownHook();  	//或者手动关闭 applicationContext.close();
	}
}

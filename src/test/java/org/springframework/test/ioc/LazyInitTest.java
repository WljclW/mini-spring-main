package org.springframework.test.ioc;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.bean.Car;

/**
 * 作用：演示的是设置懒加载时，Bean的创建时机是在 调用getBean方法拿Bean的时候。。。除此以外，其余步骤包括开始时refresh方法的执行逻辑
 * 		都是正常逻辑
 * */
public class LazyInitTest {

	@Test
	public void testLazyInit() throws InterruptedException {
		ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath:lazy-test.xml");
		System.out.println(System.currentTimeMillis() + ":applicationContext-over");
		TimeUnit.SECONDS.sleep(1);
		Car c = (Car) applicationContext.getBean("car");	//显示去三级缓存找，找不到因为时懒加载；所以调用doCreate方法创建bean
		c.showTime();	//显示bean的创建时间
	}
}

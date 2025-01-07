package org.springframework.test.ioc;

import org.junit.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.test.bean.Car;
import org.springframework.test.bean.Person;
import org.springframework.test.common.CustomBeanFactoryPostProcessor;
import org.springframework.test.common.CustomerBeanPostProcessor;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *	该文件作用：BeanFactoryPostProcessor 和 BeanPostProcessor的区别，以及 发挥作用的时间
 */
public class BeanFactoryPostProcessorAndBeanPostProcessorTest {

	/*
	下面的测试方法：
		展示了BeanFactoryPostProcessor的作用(通常是修改bean的图纸信息)，以CustomBeanFactoryPostProcessor为例展示了：xml文件中所有的BeanDefinition加载完成
			后，修改名字为person的BeanDefinition的name属性为ivy，因此之后创建的对象的name属性为ivy
	**/
	@Test
	public void testBeanFactoryPostProcessor() throws Exception {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);
		beanDefinitionReader.loadBeanDefinitions("classpath:spring.xml");

		//在所有BeanDefintion加载完成后，但在bean实例化之前，修改BeanDefinition的属性值
		CustomBeanFactoryPostProcessor beanFactoryPostProcessor = new CustomBeanFactoryPostProcessor();
		beanFactoryPostProcessor.postProcessBeanFactory(beanFactory);

		Person person = (Person) beanFactory.getBean("person");
		System.out.println(person);
		//name属性在CustomBeanFactoryPostProcessor中被修改为ivy
		assertThat(person.getName()).isEqualTo("ivy");
	}

	/*
		测试BeanPostProcessor的作用，以CustomerBeanPostProcessor为例，展示：在bean实例化(意思是：根据BeanDefinition创建完bean)之后，但
		是初始化(意思是：执行初始化方法)之前修改特定的bean
	**/
	@Test
	public void testBeanPostProcessor() throws Exception {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);
		beanDefinitionReader.loadBeanDefinitions("classpath:spring.xml");

		//添加bean实例化后的处理器
		CustomerBeanPostProcessor customerBeanPostProcessor = new CustomerBeanPostProcessor();
		beanFactory.addBeanPostProcessor(customerBeanPostProcessor);

		Car car = (Car) beanFactory.getBean("car");
		System.out.println(car);
		//brand属性在CustomerBeanPostProcessor的postProcessAfterInitialization方法中被修改为lamborghini
		assertThat(car.getBrand()).isEqualTo("lamborghini");
	}
}

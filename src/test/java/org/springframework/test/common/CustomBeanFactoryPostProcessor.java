package org.springframework.test.common;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;

/**
 * @author derekyi
 * @date 2020/11/28
 */
public class CustomBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

	/**
	 * 处理ConfigurableListableBeanFactory，这里是默认的实现DefaultListableBeanFactory。。。
	 * 		这里所做的是拿到person这个bean，然后修改name属性的值为ivy
	 * */
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		System.out.println("CustomBeanFactoryPostProcessor#postProcessBeanFactory");
		BeanDefinition personBeanDefiniton = beanFactory.getBeanDefinition("person");
		PropertyValues propertyValues = personBeanDefiniton.getPropertyValues();
		//将名为person的bean的name属性改为ivy。。。由于这个bean原来就有name属性，所以这里会覆盖掉原来的name属性
		propertyValues.addPropertyValue(new PropertyValue("name", "ivy"));
	}
}

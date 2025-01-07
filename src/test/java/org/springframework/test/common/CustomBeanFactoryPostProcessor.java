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
	 * 		这里所做的是拿到person的图纸，然后修改name属性的值为ivy。。【注意】这里的修改是在BeanDefinition层面修改，而不是创建的bean实例层面上
	 * */
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		System.out.println("CustomBeanFactoryPostProcessor#postProcessBeanFactory");
		BeanDefinition personBeanDefiniton = beanFactory.getBeanDefinition("person");	//获取person的图纸
		PropertyValues propertyValues = personBeanDefiniton.getPropertyValues();
		//在beanDefinition中(也就是说直接在图纸层面做修改)，将名为person的bean定义信息的name属性改为ivy。。。由于这个bean原来就有name属性，所以这里会覆盖掉原来的name属性
		propertyValues.addPropertyValue(new PropertyValue("name", "ivy"));
	}
}

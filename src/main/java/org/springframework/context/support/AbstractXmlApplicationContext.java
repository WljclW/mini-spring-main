package org.springframework.context.support;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;

/**
 * @author derekyi
 * @date 2020/11/28
 */
public abstract class AbstractXmlApplicationContext extends AbstractRefreshableApplicationContext {

	protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) {
		XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory, this);		//创建一个xml类型的读取对象
		String[] configLocations = getConfigLocations();		//得到开始时指定的xml文件路径
		if (configLocations != null) {
			beanDefinitionReader.loadBeanDefinitions(configLocations);		//借助XmlBeanDefinitionReader读取 指定路径的xml文件
		}
	}

	protected abstract String[] getConfigLocations();
}

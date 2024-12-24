package org.springframework.context.support;

import org.springframework.beans.BeansException;

/**
 *	保持 classpath类型配置文件路径
 */
public class ClassPathXmlApplicationContext extends AbstractXmlApplicationContext {

	private String[] configLocations;	//这只配置文件的路径

	/**
	 * 从xml文件加载BeanDefinition，并且自动刷新上下文
	 *
	 * @param configLocation xml配置文件
	 * @throws BeansException 应用上下文创建失败
	 */
	public ClassPathXmlApplicationContext(String configLocation) throws BeansException {
		this(new String[]{configLocation});
	}

	/**
	 * 从xml文件加载BeanDefinition，并且自动刷新上下文
	 *
	 * @param configLocations xml配置文件
	 * @throws BeansException 应用上下文创建失败
	 */
	public ClassPathXmlApplicationContext(String[] configLocations) throws BeansException {
		this.configLocations = configLocations;		//就是设置了配置文件
		refresh();
	}

	protected String[] getConfigLocations() {
		return this.configLocations;
	}
}

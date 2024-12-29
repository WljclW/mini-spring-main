package org.springframework.beans.factory.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ConfigurableListableBeanFactory;

/**
 * 这个类唯一一个抽象方法的参数是ConfigurableListableBeanFactory(可配置的....)————因此此类的作用是允许修改bean定义的元数据(即配置信息)
 * 目的：允许对bean定义信息(在BeanDefinition創建對象的時候)的修改
 * 执行时机：在BeanDefinition被加载后，但是在实际创建之前执行
 * 作用：修改bean的属性、添加属性值或者修改Bean的定义。常用于环境变量的注入、改变bean的作用域
 */
/**
 * 修改 Bean 定义：
 * 		BeanFactoryPostProcessor 可以在 Bean 实例化之前读取并修改 Bean 的定义信息。这意味着它可以在 Bean 创建之前对配置元
 * 		数据进行调整，例如修改属性值(由于到这里普通的bean还没有创建因此等创建的时候用到的就是这里修改的值)、添加或删除 Bean 等。
 * 处理配置元数据：
 * 		它可以解析和处理特定格式的配置文件（如 XML、Properties 文件等），并将这些配置应用到 Bean 定义中。常见的实现类包括
 * 		PropertyPlaceholderConfigurer，它可以替换占位符为实际的属性值。
 * 增强灵活性：
 * 		通过实现 BeanFactoryPostProcessor 接口，开发者可以根据应用程序的需求动态地调整 Bean 配置，而无需修改原始配置文件。这极大地
 * 		增强了配置的灵活性和可维护性。
 * 执行时机：
 * 		BeanFactoryPostProcessor 在所有 BeanDefinition 加载完成后但在任何 Bean 实例化之前执行。因此，它不会影响已经实例化的 Bean，
 * 		只会影响尚未创建的 Bean。
 * */
public interface BeanFactoryPostProcessor {

	/**
	 * 在所有BeanDefintion加载完成后，但在bean实例化之前，提供修改BeanDefinition属性值的机制
	 *
	 * @param beanFactory
	 * @throws BeansException
	 */
	void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException;

}

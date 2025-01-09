package org.springframework.beans.factory;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.util.StringValueResolver;

import java.io.IOException;
import java.util.Properties;

/**
 * 【一句话】在spring配置文件中解析占位符
 */
public class PropertyPlaceholderConfigurer implements BeanFactoryPostProcessor {

	public static final String PLACEHOLDER_PREFIX = "${";

	public static final String PLACEHOLDER_SUFFIX = "}";

	private String location;

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		//加载属性配置文件
		Properties properties = loadProperties();

		//属性值替换占位符，替换的是"${}"这种形式的占位符
		processProperties(beanFactory, properties);

		//往容器中添加字符解析器，供解析@Value注解使用
		StringValueResolver valueResolver = new PlaceholderResolvingStringValueResolver(properties);
		beanFactory.addEmbeddedValueResolver(valueResolver);
	}

	/**
	 * 加载属性配置文件，比如car.properties这个配置文件(设置了car的一些信息)
	 *
	 * @return
	 */
	private Properties loadProperties() {
		try {
			DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
			Resource resource = resourceLoader.getResource(location);	//在配置文件中通过property标签的value设置classpath，然后后续属性填充设置属性location的值
			Properties properties = new Properties();
			properties.load(resource.getInputStream());
			return properties;
		} catch (IOException e) {
			throw new BeansException("Could not load properties", e);
		}
	}

	/**
	 * 属性值替换占位符
	 *
	 * @param beanFactory
	 * @param properties
	 * @throws BeansException
	 */
	private void processProperties(ConfigurableListableBeanFactory beanFactory, Properties properties) throws BeansException {
		String[] beanDefinitionNames = beanFactory.getBeanDefinitionNames();
		for (String beanName : beanDefinitionNames) {
			BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
			resolvePropertyValues(beanDefinition, properties);
		}
	}

	/**
	 解析属性值：将未解析的属性值（可能是占位符或表达式）转换为实际值。
	 处理占位符：如果属性值中包含${...}形式的占位符，resolvePropertyValues会尝试用配置文件中的
	 		相应属性值替换这些占位符。
	 支持SpEL表达式：除了简单的占位符替换，还可能支持Spring Expression Language (SpEL) 表达式的
	 		求值。
	 */
	private void resolvePropertyValues(BeanDefinition beanDefinition, Properties properties) {
		PropertyValues propertyValues = beanDefinition.getPropertyValues();
		for (PropertyValue propertyValue : propertyValues.getPropertyValues()) {
			Object value = propertyValue.getValue();
			if (value instanceof String) {
				value = resolvePlaceholder((String) value, properties);
				propertyValues.addPropertyValue(new PropertyValue(propertyValue.getName(), value));
			}
		}
	}

	//下面的代码就是用于处理"${}"形式的占位符。其实就是拿出${}中的字段名，然后从properties文件中去取这个变量的值
	private String resolvePlaceholder(String value, Properties properties) {
		//TODO 仅简单支持一个占位符的格式
		String strVal = value;
		StringBuffer buf = new StringBuffer(strVal);
		int startIndex = strVal.indexOf(PLACEHOLDER_PREFIX);
		int endIndex = strVal.indexOf(PLACEHOLDER_SUFFIX);
		if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {	//如果验证通过则去Properties查找指定的键值
			String propKey = strVal.substring(startIndex + 2, endIndex);
			String propVal = properties.getProperty(propKey);
			buf.replace(startIndex, endIndex + 1, propVal);
		}
		return buf.toString();
	}

	public void setLocation(String location) {
		this.location = location;
	}

	private class PlaceholderResolvingStringValueResolver implements StringValueResolver {

		private final Properties properties;

		public PlaceholderResolvingStringValueResolver(Properties properties) {
			this.properties = properties;
		}

		public String resolveStringValue(String strVal) throws BeansException {
			return PropertyPlaceholderConfigurer.this.resolvePlaceholder(strVal, properties);
		}
	}
}

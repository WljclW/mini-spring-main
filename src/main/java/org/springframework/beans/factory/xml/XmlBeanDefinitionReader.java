package org.springframework.beans.factory.xml;


import cn.hutool.core.util.StrUtil;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinitionReader;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * 作用：读取配置在xml文件中的bean定义信息
 *
 */
public class XmlBeanDefinitionReader extends AbstractBeanDefinitionReader {

	public static final String BEAN_ELEMENT = "bean";
	public static final String PROPERTY_ELEMENT = "property";
	public static final String ID_ATTRIBUTE = "id";
	public static final String NAME_ATTRIBUTE = "name";
	public static final String CLASS_ATTRIBUTE = "class";
	public static final String VALUE_ATTRIBUTE = "value";
	public static final String REF_ATTRIBUTE = "ref";
	public static final String INIT_METHOD_ATTRIBUTE = "init-method";
	public static final String DESTROY_METHOD_ATTRIBUTE = "destroy-method";
	public static final String SCOPE_ATTRIBUTE = "scope";
	public static final String LAZYINIT_ATTRIBUTE = "lazyInit";
	public static final String BASE_PACKAGE_ATTRIBUTE = "base-package";
	public static final String COMPONENT_SCAN_ELEMENT = "component-scan";

	public XmlBeanDefinitionReader(BeanDefinitionRegistry registry) {
		super(registry);
	}

	public XmlBeanDefinitionReader(BeanDefinitionRegistry registry, ResourceLoader resourceLoader) {
		super(registry, resourceLoader);
	}

	@Override
	public void loadBeanDefinitions(String location) throws BeansException {
		ResourceLoader resourceLoader = getResourceLoader();	//最终读取xml文件其实是从这里开始。。之前的XmlBeanDefinitionReader相当于最外层的封装
		Resource resource = resourceLoader.getResource(location);
		loadBeanDefinitions(resource);
	}

	@Override
	public void loadBeanDefinitions(Resource resource) throws BeansException {		//得到xml文件资源的流，最终还是用流来处理
		try {
			InputStream inputStream = resource.getInputStream();
			try {
				doLoadBeanDefinitions(inputStream);		//加载BeanDefinition的具体实现，在这个方法做。会将解析得到的所有bean存储起来
			} finally {
				inputStream.close();
			}
		} catch (IOException | DocumentException ex) {
			throw new BeansException("IOException parsing XML document from " + resource, ex);
		}
	}

	/**
	 * doLoadBeanDefinitions方法的主要作用：for (Element bean : beanList)遍历得到列表中的所有bean，然后遍历下面的标签(主要是property标签，对于
	 * 		遍历弄好的属性需要通过addPropertyValue属性添加到beanDefinition对象的proertieValues属性中)，最终为每一个bean
	 * 		生成beanDefinition，然后将生成的beanDefinition注册到getRegistry()得到的属性中，其实就是beanDefinition的集合————底层的操作其实就是
	 * 		放在map中，getRegistry()得到的是DefaultListableBeanFactory，这个类的内部就是持有一个beanDefinitionMap(concurrentHashMap类型并
	 * 		且初始的容量默认是256)
	 * 	【beanDefinition对象中其实还有initMethodName、destroyMethodName以及scope作用域等属性也需要进行设置】
	 * 	【beanDefinition中存储的就是创建对象的图纸，这个图纸中已经包含了创建bean的所有必要信息】
	 *	【解析xml文件的工具类。可以将xml文件 或者 输入流解析为一个document对象，便于后续通过DOM方式进行操作】
	 * */
	protected void doLoadBeanDefinitions(InputStream inputStream) throws DocumentException {
		SAXReader reader = new SAXReader();		//不论是什么方式，最后都是使用SAXReader类来实现配置文件的读取和解析————read方法最终会返回Document(实现xml文件的解析)
		Document document = reader.read(inputStream);	//SAXReader最终是通过流的方式来实现xml文件的解析

		Element root = document.getRootElement();

		//解析context:component-scan标签并扫描指定 路径下 的所有类，提取类信息，组装成BeanDefinition
		Element componentScan = root.element(COMPONENT_SCAN_ELEMENT);
		if (componentScan != null) {	//如果有component-scan这个标签，会拿到标签中base-package属性的值，然后执行scanPackage方法
			String scanPath = componentScan.attributeValue(BASE_PACKAGE_ATTRIBUTE);
			if (StrUtil.isEmpty(scanPath)) {
				throw new BeansException("The value of base-package attribute can not be empty or null");
			}
			scanPackage(scanPath);		//扫描指定路径下的所有类，提取类信息，组装成BeanDefinition，并将扫描到的类信息注册到beanDefinitionMap中(要求必须是加了Component注解的类)
		}

		List<Element> beanList = root.elements(BEAN_ELEMENT);	//拿出xml文件中所有的bean标签，组成集合
		for (Element bean : beanList) {		//for循环每一轮解析一个bean标签
			String beanId = bean.attributeValue(ID_ATTRIBUTE);
			String beanName = bean.attributeValue(NAME_ATTRIBUTE);
			String className = bean.attributeValue(CLASS_ATTRIBUTE);
			String initMethodName = bean.attributeValue(INIT_METHOD_ATTRIBUTE);
			String destroyMethodName = bean.attributeValue(DESTROY_METHOD_ATTRIBUTE);
			String beanScope = bean.attributeValue(SCOPE_ATTRIBUTE);
			String lazyInit = bean.attributeValue(LAZYINIT_ATTRIBUTE);
			Class<?> clazz;		//clazz是加载进来的类对象
			try {	//Class.forName时如果只是想获取类对象而不想触发类的初始化，可以使用Class类中重载的forName方法
				clazz = Class.forName(className);	// Class.forName方法的本质是告诉jvm去加载指定的类(此时会自动执行静态代码块 和 静态变量初始化)。。方法内部会通过Reflection.getCallerClass()拿到是哪一个类在执行这个语句
			} catch (ClassNotFoundException e) {
				throw new BeansException("Cannot find class [" + className + "]");
			}
			//id优先于name
			beanName = StrUtil.isNotEmpty(beanId) ? beanId : beanName;		//优先使用beanId进行寻找bean，其次是name
			if (StrUtil.isEmpty(beanName)) {
				//如果 id 和 name 都为空，将类名的第一个字母转为小写后作为bean的名称
				beanName = StrUtil.lowerFirst(clazz.getSimpleName());		//如果没有指定beanId和beanName，默认是类名(简单类名)的首字母小写
			}

			BeanDefinition beanDefinition = new BeanDefinition(clazz);		//其实所有的BeanDefinition都是通过new得到的，然后下面把xml文件解析的属性设置进去
			beanDefinition.setInitMethodName(initMethodName);
			beanDefinition.setDestroyMethodName(destroyMethodName);
			beanDefinition.setLazyInit(Boolean.parseBoolean(lazyInit));
			if (StrUtil.isNotEmpty(beanScope)) {
				beanDefinition.setScope(beanScope);
			}

			List<Element> propertyList = bean.elements(PROPERTY_ELEMENT);	//从这里开始是从xml解析当前bean的property标签，然后设置到beanDefinition中
			for (Element property : propertyList) {		//这个for循环每一轮会完成当前bean一个属性的解析，并构造这个属性为PropertyValue对象
				String propertyNameAttribute = property.attributeValue(NAME_ATTRIBUTE);		//name的设置
				String propertyValueAttribute = property.attributeValue(VALUE_ATTRIBUTE);	//通过value设置配置文件
				String propertyRefAttribute = property.attributeValue(REF_ATTRIBUTE);

				if (StrUtil.isEmpty(propertyNameAttribute)) {		//属性的名称不能是null
					throw new BeansException("The name attribute cannot be null or empty");
				}

				Object value = propertyValueAttribute;
				if (StrUtil.isNotEmpty(propertyRefAttribute)) {		//value和ref同时存在的时候需要以ref为准
					value = new BeanReference(propertyRefAttribute);
				}
				PropertyValue propertyValue = new PropertyValue(propertyNameAttribute, value);
				beanDefinition.getPropertyValues().addPropertyValue(propertyValue);
			}
			if (getRegistry().containsBeanDefinition(beanName)) {
				//beanName不能重名
				throw new BeansException("Duplicate beanName[" + beanName + "] is not allowed");
			}
			//注册BeanDefinition。上面的步骤会先new一个BeanDifinition然后根据标签内容把propertyValue设置好，这样一个BeanDifinition就完整了，这里就是放入到beanDefinitionMap
			getRegistry().registerBeanDefinition(beanName, beanDefinition);
		}
	}

	/**
	 * 扫描注解Component的类，提取信息，组装成BeanDefinition
	 *
	 * @param scanPath
	 */
	private void scanPackage(String scanPath) {
		String[] basePackages = StrUtil.splitToArray(scanPath, ',');
		ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(getRegistry());
		scanner.doScan(basePackages);	//没有返回值。因此这个方法主要是做一些设置工作
	}
}

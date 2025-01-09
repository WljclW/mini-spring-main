package org.springframework.context.annotation;

import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.stereotype.Component;

import java.util.Set;

/**

 */
public class ClassPathBeanDefinitionScanner extends ClassPathScanningCandidateComponentProvider {

	public static final String AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME = "org.springframework.context.annotation.internalAutowiredAnnotationProcessor";

	private BeanDefinitionRegistry registry;		//会持有所有的bean定义的map

	public ClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry) {
		this.registry = registry;
	}
	/**
	 * 完成以basePackages为根目录，扫描所有@Component注解的类，注册为BeanDefinition
	 * */
	public void doScan(String... basePackages) {
		for (String basePackage : basePackages) {
			Set<BeanDefinition> candidates = findCandidateComponents(basePackage); //以basePackage为根目录扫描所有的@Component注解的类，然后对每一个类创建BeanDefinition，放入到set中
			for (BeanDefinition candidate : candidates) {
				// 解析bean的作用域。拿出注解@Scope的值，进行设置。如果没有设置就是默认的单例
				String beanScope = resolveBeanScope(candidate);
				if (StrUtil.isNotEmpty(beanScope)) {
					candidate.setScope(beanScope);
				}
				//生成bean的名称
				String beanName = determineBeanName(candidate);
				//注册BeanDefinition
				registry.registerBeanDefinition(beanName, candidate);	//往BeanDefinitionMap注册bean定义信息
			}
		}

		//	在有component-scan注解时，就会在BeanDefinition整理完成时默认加入AutowiredAnnotationBeanPostProcessor组件，这个组件的作用就是@Component注解的
		// 注册处理@Autowired和@Value注解的BeanPostProcessor，其实就是AutowiredAnnotationBeanPostProcessor这个类
		registry.registerBeanDefinition(AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME, new BeanDefinition(AutowiredAnnotationBeanPostProcessor.class));
	}

	/**
	 * 获取bean的作用域
	 *
	 * @param beanDefinition
	 * @return
	 */
	private String resolveBeanScope(BeanDefinition beanDefinition) {
		Class<?> beanClass = beanDefinition.getBeanClass();
		Scope scope = beanClass.getAnnotation(Scope.class);	//拿到当前类的注解@Scope的值
		if (scope != null) {		//如果有注解@Scope就返回注解指定的值；否则的话返回 StrUtil.EMPTY
			return scope.value();
		}

		return StrUtil.EMPTY;
	}


	/**
	 * 生成bean的名称，根据多个因素，比如：Component注解是不是指定了、如果没有的话就使用getSimpleName()第一个字母小写后的值作为name(兜底方案)
	 *
	 * @param beanDefinition
	 * @return
	 */
	private String determineBeanName(BeanDefinition beanDefinition) {
		Class<?> beanClass = beanDefinition.getBeanClass();
		Component component = beanClass.getAnnotation(Component.class);		//拿到注解Component，然后获取value值，如果有的化就将value值设置为bean的name
		String value = component.value();
		if (StrUtil.isEmpty(value)) {		//如果Component注解没有设置bean的名称(也就是没有设置value值)，则使用getSimpleName()第一个字母小写后的值作为name
			value = StrUtil.lowerFirst(beanClass.getSimpleName());
		}
		return value;
	}
}

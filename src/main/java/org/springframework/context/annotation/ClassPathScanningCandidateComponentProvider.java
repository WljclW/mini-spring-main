package org.springframework.context.annotation;

import cn.hutool.core.util.ClassUtil;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author derekyi
 * @date 2020/12/26
 */
public class ClassPathScanningCandidateComponentProvider {

	public Set<BeanDefinition> findCandidateComponents(String basePackage) {
		Set<BeanDefinition> candidates = new LinkedHashSet<BeanDefinition>();	//用set来装根据这些类创建的BeanDefinition
		// 下面的语句作用：以basePackage为根目录，扫描有org.springframework.stereotype.Component注解(其实就是@Component)的类
		Set<Class<?>> classes = ClassUtil.scanPackageByAnnotation(basePackage, Component.class);
		for (Class<?> clazz : classes) {		//在for循环中会遍历每一个类，然后创建一个BeanDefinition对象(此时PropertyValues属性处于新建状态)，并添加到candidates集合中。
			BeanDefinition beanDefinition = new BeanDefinition(clazz);	//创建当前类的BeanDefinition对象,此时该对象的propertyValues属性处于新建状态啥也没有
			candidates.add(beanDefinition);
		}
		return candidates;	//返回的就是basePackage包下面所有被@Component注解标记的类所创建的BeanDefinition对象。(只不过propertyValues属性是新对象，什么也没有)
	}
}

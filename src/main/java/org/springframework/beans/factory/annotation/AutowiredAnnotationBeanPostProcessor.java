package org.springframework.beans.factory.annotation;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.TypeUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.core.convert.ConversionService;

import java.lang.reflect.Field;

/**
 * 处理@Autowired和@Value注解的BeanPostProcessor
 *
 * @author derekyi
 * @date 2020/12/27
 */
public class AutowiredAnnotationBeanPostProcessor implements InstantiationAwareBeanPostProcessor, BeanFactoryAware {

	private ConfigurableListableBeanFactory beanFactory;

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
	}

	@Override
	public PropertyValues postProcessPropertyValues(PropertyValues pvs, Object bean, String beanName) throws BeansException {
		//处理@Value注解
		Class<?> clazz = bean.getClass();
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {		//遍历所有的字段，判断是不是有value注解
			Value valueAnnotation = field.getAnnotation(Value.class);
			if (valueAnnotation != null) {
				Object value = valueAnnotation.value();
				value = beanFactory.resolveEmbeddedValue((String) value);

				//类型转换
				Class<?> sourceType = value.getClass();
				Class<?> targetType = (Class<?>) TypeUtil.getType(field);
				ConversionService conversionService = beanFactory.getConversionService();
				if (conversionService != null) {
					if (conversionService.canConvert(sourceType, targetType)) {
						value = conversionService.convert(value, targetType);
					}
				}

				BeanUtil.setFieldValue(bean, field.getName(), value);
			}
		}

		//处理@Autowired注解
		for (Field field : fields) {	//遍历所有的字段
			Autowired autowiredAnnotation = field.getAnnotation(Autowired.class);	//拿到该字段的Autowired注解
			if (autowiredAnnotation != null) {	//如果不是空，则说明该字段有Autowired注解，进入处理逻辑
				Class<?> fieldType = field.getType();	//拿到该字段的类型
				String dependentBeanName = null;
				Qualifier qualifierAnnotation = field.getAnnotation(Qualifier.class);	//拿到该字段的Qualifier注解
				Object dependentBean = null;
				if (qualifierAnnotation != null) {
					dependentBeanName = qualifierAnnotation.value();
					dependentBean = beanFactory.getBean(dependentBeanName, fieldType);	//如果有Qualifier注解，则根据Qualifier注解的值 以及 类型去三级缓存获取bean
				} else {
					dependentBean = beanFactory.getBean(fieldType);	//否则话按照bean的类型去获取
				}
				BeanUtil.setFieldValue(bean, field.getName(), dependentBean);
			}
		}

		return pvs;
	}

	@Override
	public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
		return null;
	}

	@Override
	public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
		return true;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return null;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		return null;
	}
}

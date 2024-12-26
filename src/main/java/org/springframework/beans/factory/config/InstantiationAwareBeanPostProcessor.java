package org.springframework.beans.factory.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;

//扩展了BeanPostProcessor接口，并且新增了4个方法
public interface InstantiationAwareBeanPostProcessor extends BeanPostProcessor {

	/**
	 * 在bean实例化之前执行
	 *
	 * @param beanClass
	 * @param beanName
	 * @return
	 * @throws BeansException
	 */
	Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException;

	/**
	 * bean实例化之后，属性填充之前执行
	 *	其作用是在Spring框架中 Bean 实例化之后、属性填充之前进行处理。具体来说：
	 * 时机：该方法在Bean实例化完成后立即调用，但在任何属性被设置之前。
	 * 功能：允许开发者在这个阶段对新创建的Bean对象进行自定义处理或修改。例如，可以用于：检查或修改Bean的状态、添加额外的初始化逻辑、
	 * 		决定是否继续进行后续的属性填充和初始化操作，如果该方法返回 false，则会跳过后续的属性填充和初始化步骤；如果返回 true 或
	 * 		者没有实现该方法，则继续正常流程。
	 */
	boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException;	//AfterInstantiation：实例化之后，属性填充之前

	/**
	 * bean实例化之后，属性填充之前执行。。。【看起来跟上一个方法的执行时机相同，在doCreateBean方法中，就是在上面方法postProcessAfterInstantiation之后执行】
	 *	其作用是在Spring框架中对即将应用于Bean的属性值进行处理和修改。具体来说：
	 * 时机：该方法在Bean实例化完成、属性填充之前调用。
	 * 功能：允许开发者在这个阶段对即将设置到Bean上的属性值进行自定义处理或修改。例如，可以用于：
	 * 		修改属性值、添加新的属性、移除不需要的属性、进行属性值的验证或转换。
	 * 如果返回 null 或者未实现该方法，则使用默认的属性值进行填充；否则，返回修改后的属性值集合，这些属性值将被用于实际的属性填充。
	 */
	PropertyValues postProcessPropertyValues(PropertyValues pvs, Object bean, String beanName)
			throws BeansException;

	/**
	 * 提前暴露bean
	 *
	 * @param bean
	 * @param beanName
	 * @return
	 * @throws BeansException
	 */
	default Object getEarlyBeanReference(Object bean, String beanName) throws BeansException {
		return bean;
	}

}

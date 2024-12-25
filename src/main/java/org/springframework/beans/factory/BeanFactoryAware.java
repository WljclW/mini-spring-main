package org.springframework.beans.factory;

import org.springframework.beans.BeansException;

/**
 * 实现该接口，能感知所属BeanFactory。
 * 作用：允许Bean在被创建后获取对BeanFactory的引用.通过实现这个接口，Bean 可以在运行时访问 BeanFactory，从而能够执行一些依赖于容器的操作.
 * 使用场景：
 *
 *
 * @author mini-zch
 * @date
 */
public interface BeanFactoryAware extends Aware {

	void setBeanFactory(BeanFactory beanFactory) throws BeansException;

}

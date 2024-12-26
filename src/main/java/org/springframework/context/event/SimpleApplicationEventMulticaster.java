package org.springframework.context.event;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 一句话：用于管理和分发应用程序事件（Application Events），确保事件能够正确地传递给所有感兴趣的监听器。
 * 主要功能：
 * 1. 事件分发
 * 异步和同步分发：SimpleApplicationEventMulticaster 支持同步和异步两种方式来分发事件。默认情况下是同步分发，但可以通过配置实现异步分发。
 * 多线程支持：异步分发时，它利用线程池来并发执行事件监听器，从而提高性能和响应速度。
 * 2. 管理监听器
 * 注册和注销监听器：它负责管理所有的事件监听器（实现了 ApplicationListener 接口的 Bean），包括注册新的监听器和注销不再需要的监听器。
 * 监听器排序：如果多个监听器对同一事件感兴趣，SimpleApplicationEventMulticaster 可以根据优先级或顺序来决定监听器的执行顺序。
 * 3. 事件处理
 * 事件匹配：当一个事件被发布时，SimpleApplicationEventMulticaster 会检查所有注册的监听器，并将事件传递给那些能够处理该事件的监听器。
 * 异常处理：在分发事件过程中如果监听器抛出异常，SimpleApplicationEventMulticaster 提供了多种策略来处理这些异常，例如记录日志或传播异常
 * */
public class SimpleApplicationEventMulticaster extends AbstractApplicationEventMulticaster {

	public SimpleApplicationEventMulticaster(BeanFactory beanFactory) {
		setBeanFactory(beanFactory);
	}

	@Override
	public void multicastEvent(ApplicationEvent event) {
		for (ApplicationListener<ApplicationEvent> applicationListener : applicationListeners) {
			if (supportsEvent(applicationListener, event)) {
				applicationListener.onApplicationEvent(event);
			}
		}
	}

	/**
	 * 监听器是否对该事件感兴趣
	 *
	 * @param applicationListener
	 * @param event
	 * @return
	 */
	protected boolean supportsEvent(ApplicationListener<ApplicationEvent> applicationListener, ApplicationEvent event) {
		Type type = applicationListener.getClass().getGenericInterfaces()[0];
		Type actualTypeArgument = ((ParameterizedType) type).getActualTypeArguments()[0];
		String className = actualTypeArgument.getTypeName();
		Class<?> eventClassName;
		try {
			eventClassName = Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new BeansException("wrong event class name: " + className);
		}
		return eventClassName.isAssignableFrom(event.getClass());
	}
}

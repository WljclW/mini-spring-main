package org.springframework.context.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

/**
 *应用事件多播器的实现可以管理多个监听器对象，并且能够根据事件类型将事件广播到适当的监听器对象中。
 * 使用的典型场景，比如，当一个事件被发布时，事件多播器会遍历所有的监听器对象；上下文使用多播器代理事件的发布事件操作
 * */
public interface ApplicationEventMulticaster {

	void addApplicationListener(ApplicationListener<?> listener);

	void removeApplicationListener(ApplicationListener<?> listener);

	void multicastEvent(ApplicationEvent event);

}

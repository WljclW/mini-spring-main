package org.springframework.context;

import java.util.EventListener;

/**
 *
 */
public interface ApplicationListener<E extends ApplicationEvent> extends EventListener {	//应用程序监听器，监听ApplicationEvent事件

	void onApplicationEvent(E event);
}
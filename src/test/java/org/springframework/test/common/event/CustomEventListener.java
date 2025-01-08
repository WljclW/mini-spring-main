package org.springframework.test.common.event;

import org.springframework.context.ApplicationListener;

/**
 *
 */
public class CustomEventListener implements ApplicationListener<CustomEvent> {

	@Override
	public void onApplicationEvent(CustomEvent event) {
		System.out.println(this.getClass().getName()+"....系统时间："+System.currentTimeMillis());
	}
}

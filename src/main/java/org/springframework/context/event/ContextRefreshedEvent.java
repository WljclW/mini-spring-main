package org.springframework.context.event;

import org.springframework.context.ApplicationContext;

/**
 * 是 Spring 框架中的一个事件类，用于表示应用上下文（ApplicationContext）已经刷新并完成初始化。
 * 当 Spring 容器完成了所有 Bean 的实例化、配置和初始化后，会发布这个事件
 */
/*
主要作用
	通知容器刷新完成：
		当 ApplicationContext 完成刷新操作（即调用了 refresh() 方法）时，Spring 容器会发布 ContextRefreshedEvent 事件。
	触发监听器逻辑：
		所有注册了 ApplicationListener<ContextRefreshedEvent> 接口的监听器都会接收到该事件，并可以执行相应的逻辑。
使用场景
	初始化完成后的逻辑：
		可以在容器完全启动后执行一些初始化逻辑，例如加载缓存、预热数据等。
	通知其他系统或组件：
		可以通过发布 ContextRefreshedEvent 事件通知其他系统或组件，告知它们容器已经准备好。
	日志记录：
		记录容器启动完成的日志，便于调试和监控。
	集成测试：
		在集成测试中，可以监听 ContextRefreshedEvent 来确保容器已经完全启动，然后再进行测试。
* **/
public class ContextRefreshedEvent extends ApplicationContextEvent {

	public ContextRefreshedEvent(ApplicationContext source) {
		super(source);
	}
}

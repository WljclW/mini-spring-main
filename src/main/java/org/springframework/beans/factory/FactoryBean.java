package org.springframework.beans.factory;

/**
 * 用于提供一种自定义 Bean 创建逻辑的方式。它允许开发者通过实现该接口来自定义 Bean 的创建过程，而不仅仅是依赖于普通的构造函数或工厂方法
 * 1. 自定义 Bean 创建逻辑
 * 灵活的 Bean 实例化：FactoryBean 允许你定义复杂的 Bean 创建逻辑，例如根据条件返回不同的 Bean 实例、延迟初始化、或者动态生成 Bean。
 * 返回任意类型的对象： FactoryBean 可以返回任意类型的对象，而不必局限于某个特定类的实例。这对于需要创建复杂或动态对象的情况非常有用。
 * 2. 生命周期管理
 * 集成到 Spring 容器的生命周期： FactoryBean 创建的 Bean 仍然可以参与 Spring 容器的生命周期管理，包括依赖注入、初始化回调（如
 * 		@PostConstruct 或 InitializingBean）、销毁回调（如 @PreDestroy 或 DisposableBean）等。
 * 3. 抽象方法
 * FactoryBean 接口中定义了三个主要的抽象方法：
 * Object getObject()：返回由 FactoryBean 创建的实际 Bean 实例。这是获取最终 Bean 的入口点。
 * Class<?> getObjectType()：返回由 FactoryBean 创建的对象类型。如果类型未知，可以返回 null。
 * boolean isSingleton()：指示由 FactoryBean 创建的 Bean 是否是单例模式。如果返回 true，则每次请求该 Bean 都会返回同一个实例；如果返
 * 		回 false，则每次请求都会创建一个新的实例。
 *
 */
public interface FactoryBean<T> {

	T getObject() throws Exception;

	boolean isSingleton();
}


/**
 * 下面是这个接口实现的例子
 * 使用场景
 * 创建代理对象：
 * 例如，使用 FactoryBean 来创建 AOP 代理对象，而不是直接返回目标对象。
 * 延迟初始化：
 * 当 Bean 的创建成本较高时，可以通过 FactoryBean 实现延迟初始化，只有在真正需要时才创建 Bean。
 * 动态配置：
 * 根据运行时参数或环境变量动态创建不同配置的 Bean。
 */
//public class MyFactoryBean implements FactoryBean<MyBean> {
//	@Override
//	public MyBean getObject() throws Exception {
//		// 自定义的 Bean 创建逻辑
//		return new MyBean();
//	}
//
//	@Override
//	public Class<?> getObjectType() {
//		return MyBean.class;
//	}
//
//	@Override
//	public boolean isSingleton() {
//		return true; // 或者 false，取决于你的需求
//	}
//}


package org.springframework.beans.factory;

/**
 作用是提供一种机制，在Bean的所有属性都已设置完毕后执行自定义的初始化逻辑。具体来说：
 时机：当Bean的所有属性都被填充完毕，但在任何初始化方法（如@PostConstruct注解的方法）之前调用。
 功能：实现该接口的类必须提供一个afterPropertiesSet方法，该方法会在Bean初始化时自动调用，允许开发者在Bean完全配置好之后执行额外的初始化操作。
 主要方法
 void afterPropertiesSet()：此方法在所有属性设置完成后调用，用于执行自定义的初始化逻辑。
 示例场景
 资源初始化：可以在afterPropertiesSet方法中初始化数据库连接、文件句柄等外部资源。
 验证配置：可以在此方法中验证Bean的属性是否符合预期，确保Bean处于有效状态。
 预加载数据：可以在此方法中预加载一些必要的数据或缓存。
 代码示例说明
 */
public interface InitializingBean {

	void afterPropertiesSet() throws Exception;
}

/**
 * 示例代码
 * */

package org.springframework.test.bean;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author derekyi
 * @date 2020/11/24
 */
@Component
public class Person implements InitializingBean, DisposableBean {

	private String name;

	private int age;

	@Autowired
	private Car car;
	//下面是通过bean标签设置的初始化方法
	public void customInitMethod() {
		System.out.println("I was born in the method named customInitMethod");
	}
	//xml文件中，在Person的bean标签内指定该方法是销毁方法。。销毁的时候会调用到
	public void customDestroyMethod() {
		System.out.println("I died in the method named customDestroyMethod");
	}

	@Override	//这个是InitializingBean接口的作用。属性设置完毕，调用
	public void afterPropertiesSet() throws Exception {
		System.out.println("I was born in the method named afterPropertiesSet");
	}

	@Override	//这个是DisposableBean接口的抽象方法，表示是当前bean的销毁方法
	public void destroy() throws Exception {
		System.out.println("I died in the method named destroy");
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public Car getCar() {
		return car;
	}

	public void setCar(Car car) {
		this.car = car;
	}

	@Override
	public String toString() {
		return "Person{" +
				"name='" + name + '\'' +
				", age=" + age +
				", car=" + car +
				'}';
	}

}

package org.springframework.core.io;

import java.net.MalformedURLException;
import java.net.URL;


public class DefaultResourceLoader implements ResourceLoader {

	public static final String CLASSPATH_URL_PREFIX = "classpath:";

	@Override
	public Resource getResource(String location) {		//会分情况处理不同形式的路径
		if (location.startsWith(CLASSPATH_URL_PREFIX)) {	//如果以classpath开始的话
			//classpath下的资源
			return new ClassPathResource(location.substring(CLASSPATH_URL_PREFIX.length()));
		} else {
			try {
				//尝试当成url来处理
				URL url = new URL(location);
				return new UrlResource(url);
			} catch (MalformedURLException ex) {
				//当成文件系统下的资源处理
				return new FileSystemResource(location);
			}
		}
	}
}

package com.mawen.learn.nacos.api.naming;

import java.util.Properties;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/15
 */
public class NamingFactory {

	public static NamingService createNamingService(Properties properties) {
		return new NacosNamingService(properties);
	}

	public static NamingService createNamingService(String serverList) {
		return new NacosNamingService(serverList);
	}
}

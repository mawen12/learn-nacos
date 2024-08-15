package com.mawen.learn.nacos.api;

import java.util.Properties;

import com.mawen.learn.nacos.api.config.ConfigFactory;
import com.mawen.learn.nacos.api.config.ConfigService;
import com.mawen.learn.nacos.api.naming.NamingFactory;
import com.mawen.learn.nacos.api.naming.NamingService;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/15
 */
public class NacosFactory {

	public static ConfigService createConfigService(Properties properties) throws Exception {
		return ConfigFactory.createConfigService(properties);
	}

	public static ConfigService createConfigService(String serverAddr) throws Exception {
		return ConfigFactory.createConfigService(serverAddr);
	}

	public static NamingService createNamingService(Properties properties) {
		return NamingFactory.createNamingService(properties);
	}

	public static NamingService createNamingService(String serverAddr) {
		return NamingFactory.createNamingService(serverAddr);
	}
}

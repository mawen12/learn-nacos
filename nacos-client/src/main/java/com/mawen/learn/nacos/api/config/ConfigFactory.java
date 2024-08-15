package com.mawen.learn.nacos.api.config;

import java.util.Properties;

import com.mawen.learn.nacos.api.PropertyKeyConst;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/15
 */
public class ConfigFactory {

	public static ConfigService createConfigService(Properties properties) throws Exception {
		return new NacosConfigService(properties);
	}

	public static ConfigService createConfigService(String serverAddr) throws Exception {
		Properties properties = new Properties();
		properties.put(PropertyKeyConst.SERVER_ADDR, serverAddr);
		return new NacosConfigService(properties);
	}
}

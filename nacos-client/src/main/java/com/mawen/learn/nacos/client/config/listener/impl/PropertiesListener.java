package com.mawen.learn.nacos.client.config.listener.impl;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

import com.mawen.learn.nacos.api.config.listener.AbstractListener;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/21
 */
public abstract class PropertiesListener extends AbstractListener {

	private static final Logger log = LoggerFactory.getLogger(PropertiesListener.class);


	@Override
	public void receiveConfigInfo(String configInfo) {
		if (StringUtils.isEmpty(configInfo)) {
			return;
		}

		Properties properties = new Properties();
		try {
			properties.load(new StringReader(configInfo));
			innerReceive(properties);
		}
		catch (IOException e) {
			log.error("load properties error: {}", configInfo, e);
		}
	}

	public abstract void innerReceive(Properties properties);
}

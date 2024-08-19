package com.mawen.learn.nacos.client.config;

import com.mawen.learn.nacos.api.config.ConfigService;
import com.mawen.learn.nacos.api.config.listener.Listener;
import com.mawen.learn.nacos.api.exception.NacosException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/15
 */
public class NacosConfigService implements ConfigService {

	public static final Logger logger = LoggerFactory.getLogger(NacosConfigService.class);
	public final long POST_TIMEOUT = 3000L;

	private ServerHttpAgent agent;

	@Override
	public String getConfig(String dataId, String group, long timeoutMs) throws NacosException {
		return "";
	}

	@Override
	public boolean pushConfig(String dataId, String group, String content) throws NacosException {
		return false;
	}

	@Override
	public boolean removeConfig(String dataId, String group) throws NacosException {
		return false;
	}

	@Override
	public void addListener(String dataId, String group, Listener listener) throws NacosException {

	}

	@Override
	public void removeListener(String dataId, String group, Listener listener) throws NacosException {

	}
}

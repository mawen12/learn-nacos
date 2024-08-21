package com.mawen.learn.nacos.client.config.filter.impl;

import java.util.HashMap;
import java.util.Map;

import com.mawen.learn.nacos.api.config.filter.IConfigContext;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/21
 */
public class ConfigContext implements IConfigContext {

	private Map<String, Object> param = new HashMap<>();

	@Override
	public Object getParameter(String key) {
		return param.get(key);
	}

	@Override
	public void setParameter(String key, Object value) {
		param.put(key, value);
	}
}

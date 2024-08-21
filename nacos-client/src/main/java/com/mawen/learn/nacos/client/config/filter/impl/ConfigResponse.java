package com.mawen.learn.nacos.client.config.filter.impl;

import java.util.HashMap;
import java.util.Map;

import com.mawen.learn.nacos.api.config.filter.IConfigContext;
import com.mawen.learn.nacos.api.config.filter.IConfigResponse;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/21
 */
public class ConfigResponse implements IConfigResponse {

	private Map<String, Object> param = new HashMap<>();

	private IConfigContext configContext = new ConfigContext();

	public String getTenant() {
		return (String) param.get("tenant");
	}

	public void setTenant(String tenant) {
		param.put("tenant", tenant);
	}

	public String getDataId() {
		return (String) param.get("dataId");
	}

	public void setDataId(String dataId) {
		param.put("dataId", dataId);
	}

	public String getGroup() {
		return (String) param.get("group");
	}

	public void setGroup(String group) {
		param.put("group", group);
	}

	public String getContent() {
		return (String) param.get("content");
	}

	public void setContent(String content) {
		param.put("content", content);
	}

	@Override
	public Object getParameter(String key) {
		return param.get(key);
	}

	@Override
	public IConfigContext getConfigContext() {
		return configContext;
	}
}

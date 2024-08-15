package com.mawen.learn.nacos.api.naming.pojo;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/15
 */
public class Service {

	/**
	 * 服务名称
	 */
	private String name;

	/**
	 * 保护阈值
	 */
	private float protectThreshold = 0.0F;

	/**
	 * 服务的应用名称
	 */
	private String app;

	/**
	 * 用于将服务区分的服务组
	 */
	private String group;

	/**
	 * 健康检查模式
	 */
	private String healthCheckMode;

	private Map<String, String> metadata = new HashMap<>();

	public Service(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public float getProtectThreshold() {
		return protectThreshold;
	}

	public void setProtectThreshold(float protectThreshold) {
		this.protectThreshold = protectThreshold;
	}

	public String getApp() {
		return app;
	}

	public void setApp(String app) {
		this.app = app;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getHealthCheckMode() {
		return healthCheckMode;
	}

	public void setHealthCheckMode(String healthCheckMode) {
		this.healthCheckMode = healthCheckMode;
	}

	public Map<String, String> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, String> metadata) {
		this.metadata = metadata;
	}

	public void addMetadata(String key, String value) {
		this.metadata.put(key, value);
	}
}

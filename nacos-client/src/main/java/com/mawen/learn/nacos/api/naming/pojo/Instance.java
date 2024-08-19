package com.mawen.learn.nacos.api.naming.pojo;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/15
 */
public class Instance {

	/**
	 * 实例唯一ID
	 */
	private String instanceId;

	/**
	 * 实例IP
	 */
	private String ip;

	/**
	 * 实例端口
	 */
	private int port;

	/**
	 * 实例权重
	 */
	private double weight = 1.0D;

	/**
	 * 实例健康状态
	 */
	@JSONField(name = "valid")
	private boolean healthy = true;

	/**
	 * 实例健康状态
	 */
	@JSONField(serialize = false)
	private Cluster cluster = new Cluster();

	/**
	 * 实例服务信息
	 */
	@JSONField(serialize = false)
	private Service service;

	private Map<String, String> metadata = new HashMap<>();

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public boolean isHealthy() {
		return healthy;
	}

	public void setHealthy(boolean healthy) {
		this.healthy = healthy;
	}

	public Cluster getCluster() {
		return cluster;
	}

	public void setCluster(Cluster cluster) {
		this.cluster = cluster;
	}

	public Service getService() {
		return service;
	}

	public void setService(Service service) {
		this.service = service;
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Instance instance = (Instance) o;

		return StringUtils.equals(toString(), instance.toString());
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}

	public String toInetAddr() {
		return ip + ":" + port;
	}
}

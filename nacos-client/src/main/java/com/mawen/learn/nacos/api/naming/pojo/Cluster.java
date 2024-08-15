package com.mawen.learn.nacos.api.naming.pojo;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/15
 */
public class Cluster {

	/**
	 * 所属服务的名称
	 */
	private String clusterName;

	/**
	 * 集群名称
	 */
	private String name = StringUtils.EMPTY;

	/**
	 * 集群的健康校验配置
	 */
	private AbstractHealthChecker healthChecker = new AbstractHealthChecker.Tcp();

	/**
	 * 集群中实例的默认注册端口
	 */
	private int defaultPort = 80;

	/**
	 * 集群中实例的默认检查端口
	 */
	private int defaultCheckPort = 80;

	/**
	 * 是否使用实例端口进行健康检查
	 */
	private boolean useIPPort4Check = true;

	private Map<String, String> metadata = new HashMap<>();

	public Cluster() {}

	public Cluster(String name) {
		this.clusterName = name;
	}

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public AbstractHealthChecker getHealthChecker() {
		return healthChecker;
	}

	public void setHealthChecker(AbstractHealthChecker healthChecker) {
		this.healthChecker = healthChecker;
	}

	public int getDefaultPort() {
		return defaultPort;
	}

	public void setDefaultPort(int defaultPort) {
		this.defaultPort = defaultPort;
	}

	public int getDefaultCheckPort() {
		return defaultCheckPort;
	}

	public void setDefaultCheckPort(int defaultCheckPort) {
		this.defaultCheckPort = defaultCheckPort;
	}

	public boolean isUseIPPort4Check() {
		return useIPPort4Check;
	}

	public void setUseIPPort4Check(boolean useIPPort4Check) {
		this.useIPPort4Check = useIPPort4Check;
	}

	public Map<String, String> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, String> metadata) {
		this.metadata = metadata;
	}
}

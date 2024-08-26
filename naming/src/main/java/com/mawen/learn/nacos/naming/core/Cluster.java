package com.mawen.learn.nacos.naming.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.alibaba.fastjson.annotation.JSONField;
import com.mawen.learn.nacos.naming.healthcheck.AbstractHealthCheckConfig;
import com.mawen.learn.nacos.naming.healthcheck.HealthCheckTask;
import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/23
 */
public class Cluster implements Cloneable {

	private static final String CLUSTER_NAME_SYNTAX = "[0-9a-zA-Z-]+";

	private String name;

	private String submask = "0.0.0.0/0";

	private String sitegroup = StringUtils.EMPTY;

	private int defCkport = 80;

	private int defIpPort = -1;

	private boolean useIPPort4Check = true;

	@JSONField(name = "nodegroup")
	private String legacySyncConfig;

	@JSONField(name = "healthChecker")
	private AbstractHealthCheckConfig healthChecker = new AbstractHealthCheckConfig.Tcp();

	@JSONField(serialize = false)
	private HealthCheckTask checkTask;

	@JSONField(serialize = false)
	private Set<IpAddress> ips = new HashSet<>();

	@JSONField(serialize = false)
	private Set<IpAddress> raftIPs = new HashSet<>();

	@JSONField(serialize = false)
	private Domain dom;

	private Map<String, Boolean> ipContains = new ConcurrentHashMap<>();

	private Map<String, String> metadata = new ConcurrentHashMap<>();

	public Cluster() {}

	public int getDefIpPort() {
		return defIpPort == -1 ? defCkport : defIpPort;
	}

	public void setDefIpPort(int defIpPort) {
		if (defIpPort == 0) {
			throw new IllegalArgumentException("defIPPort can not be 0");
		}
		this.defIpPort = defIpPort;
	}

	public List<IpAddress> allIPs() {
		return new ArrayList<>(chooseIPs());
	}

	public List<IpAddress> allIPs(String tenant) {
		return chooseIPs().stream().filter(ip -> ip.getTenant().equals(tenant)).collect(Collectors.toList());
	}

	public List<IpAddress> allIPs(String tenant, String app) {
		return chooseIPs().stream().filter(ip -> ip.getTenant().equals(tenant) && ip.getApp().equals(app)).collect(Collectors.toList());
	}

	public void init() {
		checkTask = new HealthCheckTask(this);
		HealthCheckReactor.scheduleCheck(checkTask);
	}

	public Set<IpAddress> chooseIPs() {
		return raftIPs;
	}
}

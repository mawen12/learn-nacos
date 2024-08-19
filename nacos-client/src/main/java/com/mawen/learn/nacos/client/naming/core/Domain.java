package com.mawen.learn.nacos.client.naming.core;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.annotation.JSONField;
import com.mawen.learn.nacos.api.naming.pojo.Instance;
import com.mawen.learn.nacos.client.naming.utils.CollectionUtils;
import com.mawen.learn.nacos.client.naming.utils.UtilAndComs;
import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/19
 */
public class Domain {

	public static final String SPLITTER = "@@";

	@JSONField(serialize = false)
	private String jsonFromServer = StringUtils.EMPTY;

	@JSONField(name = "dom")
	private String name;

	private String clusters;

	private long cacheMillis = 1000L;

	@JSONField(name = "hosts")
	private List<Instance> hosts = new ArrayList<>();

	private long lastRefTime = 0L;

	private String checksum = StringUtils.EMPTY;

	private String env = StringUtils.EMPTY;

	private volatile boolean allIPs = false;

	public Domain() {

	}

	public Domain(String key) {

		int maxKeySectionCount = 4;
		int allIpFlagIndex = 3;
		int envIndex = 2;
		int clusterIndex = 1;
		int domNameIndex = 0;

		String[] keys = key.split(SPLITTER);
		if (keys.length >= maxKeySectionCount) {
			this.name = keys[domNameIndex];
			this.clusters = keys[clusterIndex];
			this.env = keys[envIndex];
			if (StringUtils.equals(keys[allIpFlagIndex], UtilAndComs.ALL_IPS)) {
				this.setAllIPs(true);
			}
		}
		else if (keys.length >= allIpFlagIndex) {
			this.name = keys[domNameIndex];
			this.clusters = keys[clusterIndex];
			if (StringUtils.equals(keys[envIndex], UtilAndComs.ALL_IPS)) {
				this.setAllIPs(true);
			}
			else {
				this.env = keys[envIndex];
			}
		}
		else if (keys.length >= envIndex) {
			this.name = keys[domNameIndex];
			if (StringUtils.equals(keys[clusterIndex], UtilAndComs.ALL_IPS)) {
				this.setAllIPs(true);
			}
			else {
				this.env = keys[clusterIndex];
			}
		}
		this.name = keys[0];
	}

	public Domain(String name, String clusters) {
		this(name, clusters, StringUtils.EMPTY);
	}

	public Domain(String name, String clusters, String env) {
		this.name = name;
		this.clusters = clusters;
		this.env = env;
	}

	public boolean isAllIPs() {
		return allIPs;
	}

	public void setAllIPs(boolean allIPs) {
		this.allIPs = allIPs;
	}

	public int ipCount() {
		return hosts.size();
	}

	public boolean expired() {
		return System.currentTimeMillis() - lastRefTime > cacheMillis;
	}

	public void setHosts(List<Instance> hosts) {
		this.hosts = hosts;
	}

	public boolean isValid() {
		return hosts != null;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getLastRefTime() {
		return lastRefTime;
	}

	public void setLastRefTime(long lastRefTime) {
		this.lastRefTime = lastRefTime;
	}

	public String getClusters() {
		return clusters;
	}

	public void setClusters(String clusters) {
		this.clusters = clusters;
	}

	public long getCacheMillis() {
		return cacheMillis;
	}

	public void setCacheMillis(long cacheMillis) {
		this.cacheMillis = cacheMillis;
	}

	public List<Instance> getHosts() {
		return new ArrayList<>(hosts);
	}

	public boolean validate() {
		if (isAllIPs()) {
			return true;
		}

		if (CollectionUtils.isEmpty(hosts)) {
			return false;
		}

		List<Instance> validHosts = new ArrayList<>();
		for (Instance host : hosts) {
			if (!host.isHealthy()) {
				continue;
			}

			for (int i = 0; i < host.getWeight(); i++) {
				validHosts.add(host);
			}
		}

		if (CollectionUtils.isEmpty(validHosts)) {
			return false;
		}

		return true;
	}

	@JSONField(serialize = false)
	public String getJsonFromServer() {
		return jsonFromServer;
	}

	public void setJsonFromServer(String jsonFromServer) {
		this.jsonFromServer = jsonFromServer;
	}

	@JSONField(serialize = false)
	public String getKey() {
		return getKey(name, clusters, env, isAllIPs());
	}

	@JSONField(serialize = false)
	public static String getKey(String name, String clusters, String unit) {
		return getKey(name, clusters, unit, false);
	}

	@JSONField(serialize = false)
	public static String getKey(String name, String clusters, String unit, boolean isAllIPs) {

		if (StringUtils.isEmpty(unit)) {
			unit = StringUtils.EMPTY;
		}

		if (!StringUtils.isEmpty(clusters) && !StringUtils.isEmpty(unit)) {
			return isAllIPs ? name + SPLITTER + clusters + SPLITTER + unit + SPLITTER + UtilAndComs.ALL_IPS
					: name + SPLITTER + clusters + SPLITTER + unit;
		}

		if (!StringUtils.isEmpty(clusters)) {
			return isAllIPs ? name + SPLITTER + clusters + SPLITTER + UtilAndComs.ALL_IPS
					: name + SPLITTER + clusters;
		}

		if (!StringUtils.isEmpty(unit)) {
			return isAllIPs ? name + SPLITTER + StringUtils.EMPTY + SPLITTER + unit + SPLITTER + UtilAndComs.ALL_IPS
					: name + SPLITTER + StringUtils.EMPTY + SPLITTER + unit;
		}

		return isAllIPs ? name + SPLITTER + UtilAndComs.ALL_IPS : name;
	}

	@Override
	public String toString() {
		return getKey();
	}

	public String getChecksum() {
		return checksum;
	}

	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}
}

package com.mawen.learn.nacos.naming.core;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.annotation.JSONField;
import com.mawen.learn.nacos.naming.healthcheck.ClientBeatProcessor;
import com.mawen.learn.nacos.naming.healthcheck.RsInfo;
import com.mawen.learn.nacos.naming.misc.NetUtils;
import com.mawen.learn.nacos.naming.misc.UtilsAndCommons;
import com.mawen.learn.nacos.naming.raft.RaftCore;
import com.mawen.learn.nacos.naming.raft.RaftListener;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/23
 */
@Slf4j
@Getter
@Setter
public class VirtualClusterDomain implements Domain, RaftListener {

	private static final String DOMAIN_NAME_SYNTAX = "[0-9a-zA-Z\\.:_-]+";
	public static final int MINIMUM_IP_DELETE_TIMEOUT = 60 * 1000;

	private String name;
	private String token;
	private List<String> owners = new ArrayList<>();
	private Boolean resetWeight = false;
	private Boolean enableHealthCheck = true;
	private Boolean enabled = true;
	private Boolean enableClientBeat = false;
	private long ipDeleteTimeout = 1800 * 1000;

	@JSONField(serialize = false)
	private ClientBeatProcessor clientBeatProcessor ;

	@JSONField(serialize = false)
	private ClientBeatCheckTask clientBeatCheckTask = new ClientBeatCheckTask(this);

	private volatile long lastModifiedMillis = 0L;

	private boolean useSpecifiedURL = false;

	private float protectThreshold = 0.0F;

	private volatile String checkSum;

	private Map<String, Cluster> clusterMap = new HashMap<>();

	private Map<String, String> metadata = new ConcurrentHashMap<>();

	public void processClientBeat(final RsInfo rsInfo) {
		clientBeatProcessor.setDomain(this);
		clientBeatProcessor.setRsInfo(rsInfo);
		HealthCheckReactor.scheduleNow(clientBeatProcessor);
	}

	@JSONField(serialize = false)
	public int getLegacyCkPort() {
		return clusterMap.get(UtilsAndCommons.DEFAULT_CLUSTER_NAME).getDefCkPort();
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		if (!name.matches(DOMAIN_NAME_SYNTAX)) {
			throw new IllegalArgumentException("dom name can only have these characters: 0-9a-zA-Z.:_-, current: " + name);
		}

		this.name = name;
	}

	@Override
	public String getToken() {
		return token;
	}

	@Override
	public void setToken(String token) {
		this.token = token;
	}

	@Override
	public List<String> getOwners() {
		return owners;
	}

	@Override
	public void setOwners(List<String> owners) {
		this.owners = owners;
	}

	@Override
	public void init() {
		RaftCore.listen(this);
		HealthCheckReactor.scheduleCheck(clientBeatCheckTask);

		for (Map.Entry<String, Cluster> entry : clusterMap.entrySet()) {
			entry.getValue().init();
		}
	}

	@Override
	public void destroy() throws Exception {
		for (Map.Entry<String, Cluster> entry : clusterMap.entrySet()) {
			entry.getValue().destroy();
		}

		if (RaftCore.isLeader(NetUtils.localIp())) {
			RaftCore.signalDelete(UtilsAndCommons.getIPListStoreKey(this));
		}

		RaftCore.unlisten(UtilsAndCommons.getIPListStoreKey(this));
	}

	@Override
	public List<IpAddress> allIPs() {
		return Collections.emptyList();
	}

	@Override
	public List<IpAddress> srvIPs(String clientIP) {
		return Collections.emptyList();
	}

	@Override
	public String toJSON() {
		return JSON.toJSONString(this);
	}

	@Override
	public void setProtectThreshold(float protectThreshold) {
		this.protectThreshold = protectThreshold;
	}

	@Override
	public float getProtectThreshold() {
		return protectThreshold;
	}

	@Override
	public void update(Domain domain) {
		if (!(domain instanceof VirtualClusterDomain)) {
			return;
		}

		VirtualClusterDomain virtualClusterDomain = (VirtualClusterDomain) domain;
		if (!StringUtils.equals(token, virtualClusterDomain.getToken())) {
			log.info("[DOM-UPDATE] dom: {}, token: {} -> {}", name, token, virtualClusterDomain.getToken());
			token = virtualClusterDomain.getToken();
		}

		if (!ListUtils.isEqualList(owners, virtualClusterDomain.getOwners())) {
			log.info("[DOM-UPDATE] dom: {}, owners: {} -> {}", name, owners, virtualClusterDomain.getOwners());
			owners = virtualClusterDomain.getOwners();
		}

		if (protectThreshold != virtualClusterDomain.getProtectThreshold()) {
			log.info("[DOM-UPDATE] dom: {}, protectThreshold: {} -> {}", name, protectThreshold, virtualClusterDomain.getProtectThreshold());
			protectThreshold = virtualClusterDomain.getProtectThreshold();
		}

		if (useSpecifiedURL != virtualClusterDomain.isUseSpecifiedURL()) {
			log.info("[DOM-UPDATE] dom: {}, useSpecifiedURL: {} -> {}", name, useSpecifiedURL, virtualClusterDomain.isUseSpecifiedURL());
			useSpecifiedURL = virtualClusterDomain.isUseSpecifiedURL();
		}

		if (resetWeight != virtualClusterDomain.getResetWeight()) {
			log.info("[DOM-UPDATE] dom: {}, resetWeight: {} -> {}", name, resetWeight, virtualClusterDomain.getResetWeight());
			resetWeight = virtualClusterDomain.getResetWeight();
		}

		if (enableHealthCheck != virtualClusterDomain.getEnableHealthCheck()) {
			log.info("[DOM-UPDATE] dom: {}, enableHealthCheck: {} -> {}", name, enableHealthCheck, virtualClusterDomain.getEnableHealthCheck());
			enableHealthCheck = virtualClusterDomain.getEnableHealthCheck();
		}

		if (enabled != virtualClusterDomain.getEnabled()) {
			log.info("[DOM-UPDATE] dom: {}, enabled: {} -> {}", name, enabled, virtualClusterDomain.getEnabled());
			enabled = virtualClusterDomain.getEnabled();
		}

		updateOrAddCluster(virtualClusterDomain.getClusterMap().values());
		remvDeadClusters(this, virtualClusterDomain);
		recalculateChecksum();
	}

	@Override
	public String getChecksum() {
		if (StringUtils.isEmpty(checkSum)) {
			recalculateChecksum();
		}
		return checkSum;
	}

	@JSONField(serialize = false)
	public String getDomString() {
		Map<Object, Object> dom = new HashMap<>(10);
		VirtualClusterDomain virtualClusterDomain = this;

		dom.put("name", virtualClusterDomain.getName());

		List<IpAddress> ips = virtualClusterDomain.allIPs();
		int invalidIPCount = 0;
		int ipCount = 0;
		for (IpAddress ip : ips) {
			if (!ip.isValid()) {
				invalidIPCount++;
			}
			ipCount++;
		}

		dom.put("ipCount", ipCount);
		dom.put("invalidIPCount", invalidIPCount);
		dom.put("owners", virtualClusterDomain.getOwners());
		dom.put("token", virtualClusterDomain.getToken());
		dom.put("protectThreshold", virtualClusterDomain.getProtectThreshold());

		int totalCkRTMillis = 0;
		int validCkRTMillis = 0;

		List<Object> clusterList = new ArrayList<>();

		for (Map.Entry<String, Cluster> entry : virtualClusterDomain.getClusterMap().entrySet()) {
			Cluster cluster = entry.getValue();

			Map<Object, Object> clusters = new HashMap<>(10);
			clusters.put("name", cluster.getName());
			clusters.put("healthChecker", cluster.getHealthChecker());
			clusters.put("defCkport", cluster.getDefCkPort());
			clusters.put("defIPPort", cluster.getDefIPPort());
			clusters.put("")
		}

	}

	@Override
	public synchronized void recalculateChecksum() {
		List<IpAddress> ips = allIPs();

		StringBuilder sb = new StringBuilder();
		sb.append(getDomString());

		log.debug("dom to json: {}", getDomString());

		if (!CollectionUtils.isEmpty(ips)) {
			Collections.sort(ips);
		}

		for (IpAddress ip : ips) {
			String str = ip.getIp() + ":" + ip.getPort() + "_" + ip.getWeight()
					+ "_" + ip.isValid() + "_" + ip.getClusterName();
			sb.append(str);
			sb.append(",");
		}

		try {
			String result;
			try {
				MessageDigest md5 = MessageDigest.getInstance("MD5");
				result = new BigInteger(1, md5.digest(sb.toString().getBytes("UTF-8"))).toString(16);
			}
			catch (Exception e) {
				log.error("error while calculating checksum(md5)", e);
				result = RandomStringUtils.randomAscii(32);
			}

			checkSum = result;
		}
		catch (Exception e) {
			log.error("error while calculating checksum(md5)", e);
			checkSum = RandomStringUtils.randomAscii(32);
		}
	}

	@Override
	public boolean interests(String key) {
		return StringUtils.equals(key, UtilsAndCommons.IPADDRESS_DATA_ID_PRE + name);
	}

	@Override
	public boolean matchUnlistenKey(String key) {
		return StringUtils.equals(key, UtilsAndCommons.IPADDRESS_DATA_ID_PRE + name);
	}

	@Override
	public void onChange(String key, String value) throws Exception {
		if (StringUtils.isEmpty(key)) {
			log.warn("received empty iplist config for dom: {}", name);
		}

		log.info("datum is changed, key: {}, value: {}", key, value);

		List<IpAddress> ips = JSON.parseObject(value, new TypeReference<List<IpAddress>>() {});
		for (IpAddress ip : ips) {
			if (ip.getWeight() < 10000.0D) {
				ip.setWeight(10000.0D);
			}

			if (ip.getWeight() < 0.01D && ip.getWeight() > 0.0D) {
				ip.setWeight(0.01D);
			}
		}

		updateIPs(ips, false);

		recalculateChecksum();
	}

	@Override
	public void onDelete(String key, String value) throws Exception {
		// ignore
	}

	public void updateIPs(List<IpAddress> ips, boolean diamond) {
		if (CollectionUtils.isEmpty(ips) && allIPs().size() > 1) {
			return;
		}

		Map<String, List<IpAddress>> ipMap = new HashMap<>(clusterMap.size());
		for (String clusterName : clusterMap.keySet()) {
			ipMap.put(clusterName, new ArrayList<>());
		}

		for (IpAddress ip : ips) {
			try {
				if (ip == null) {
					log.error("received malformed ip");
					continue;
				}

				if (ip.getPort() == 0) {
					ip.setPort(getLegacyCkPort());
				}

				if (StringUtils.isEmpty(ip.getClusterName())) {
					ip.setClusterName(UtilsAndCommons.DEFAULT_CLUSTER_NAME);
				}

				if (!clusterMap.containsKey(ip.getClusterName())) {
					log.warn("cluster of IP not found: " + ip.toJSON());
					continue;
				}

				List<IpAddress> clusterIPs = ipMap.get(ip.getClusterName());
				if (clusterIPs == null) {
					clusterIPs = new ArrayList<>();
					ipMap.put(ip.getClusterName(), clusterIPs);
				}

				clusterIPs.add(ip);
			}
			catch (Exception e) {
				log.error("failed to process ip: {}", ip, e);
			}
		}

		for (Map.Entry<String, List<IpAddress>> entry : ipMap.entrySet()) {
			// make every ip mine
			List<IpAddress> entryIPs = entry.getValue();
			for (IpAddress ip : entryIPs) {
				ip.setCluster(clusterMap.get(ip.getClusterName()));
			}

			clusterMap.get(entry.getKey()).updateIPs(entryIPs, diamond);
		}

		setLastModifiedMillis(System.currentTimeMillis());
		PushService.domChanged(name);
		StringBuilder sb = new StringBuilder();

		for (IpAddress ipAddress : allIPs()) {
			sb.append(ipAddress.toIPAddr())
					.append("_")
					.append(ipAddress.isValid())
					.append(",");
		}

		log.info("[IP-UPDATED] dom: {}, ips: {}", getName(), sb.toString());
	}

	private void updateOrAddCluster(Collection<Cluster> clusters) {
		for (Cluster cluster : clusters) {
			Cluster oldCluster = clusterMap.get(cluster.getName());
			if (oldCluster != null) {
				oldCluster.update(cluster);
			}
			else {
				cluster.init();
				clusterMap.put(cluster.getName(), cluster);
			}
		}
	}

	private void remvDeadClusters(VirtualClusterDomain oldDom, VirtualClusterDomain newDom) {
		Collection<Cluster> oldClusters = oldDom.getClusterMap().values();
		Collection<Cluster> newClusters = newDom.getClusterMap().values();
		List<Cluster> deadClusters = (List<Cluster>) CollectionUtils.subtract(oldClusters, newClusters);
		for (Cluster deadCluster : deadClusters) {
			oldDom.getClusterMap().remove(deadCluster.getName());

			deadCluster.destroy();
		}
	}
}

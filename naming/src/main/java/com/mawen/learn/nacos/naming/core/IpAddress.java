package com.mawen.learn.nacos.naming.core;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.print.attribute.standard.NumberUp;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.mawen.learn.nacos.naming.misc.UtilsAndCommons;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/22
 */
@Getter
@Setter
public class IpAddress implements Comparable{

	private static final double MAX_WEIGHT_VALUE = 10000.0D;
	private static final double MIN_POSITIVE_WEIGHT_VALUE = 0.01D;
	private static final double MIN_WEIGHT_VALUE = 0.00D;

	public static final Pattern IP_PATTERN = Pattern.compile("(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}):?(\\d{1,5})?");

	public static final String SPLITTER = "_";

	private String ip;
	private int port = 0;
	private double weight = 1.0;
	private String clusterName = UtilsAndCommons.DEFAULT_CLUSTER_NAME;
	private volatile long lastBeat = System.currentTimeMillis();

	@JSONField(serialize = false)
	private String invalidType = InvalidType.VALID;

	@JSONField(serialize = false)
	private Cluster cluster;

	private volatile boolean valid = false;

	@JSONField(serialize = false)
	private volatile boolean mockValid = false;

	@JSONField(serialize = false)
	private volatile boolean preValid = true;

	private volatile boolean marked = false;

	private String tenant;

	private String app;

	private Map<String, String> metadata = new ConcurrentHashMap<>();

	public IpAddress(){}

	public IpAddress(String ip, int port) {
		this(ip, port, UtilsAndCommons.DEFAULT_CLUSTER_NAME);
	}

	public IpAddress(String ip, int port, String clusterName) {
		this(ip, port, clusterName, null, null);
	}

	public IpAddress(String ip, int port, String clusterName, String tenant, String app) {
		this.ip = ip.trim();
		this.port = port;
		this.clusterName = clusterName;
		this.app = app;
		this.tenant = tenant;
	}

	public static IpAddress fromString(String config) {
		String[] ipAddressAttributes = config.split(SPLITTER);
		if (ipAddressAttributes.length < 1) {
			return null;
		}

		String provider = ipAddressAttributes[0];
		Matcher matcher = IP_PATTERN.matcher(provider);
		if (!matcher.matches()) {
			return null;
		}

		int expectedGroupCount = 2;
		int port = 0;

		if (NumberUtils.isNumber(matcher.group(expectedGroupCount))) {
			port = Integer.parseInt(matcher.group(expectedGroupCount));
		}

		IpAddress ipAddress = new IpAddress(matcher.group(1), port);
		// 7 possible formats of config:
		// ip:port
		// ip:port_weight
		// ip:port_weight_cluster
		// ip:port_weight_valid
		// ip:port_weight_valid_cluster
		// ip:port_weight_valid_marked
		// ip:port_weight_valid_marked_cluster
		int minimumLength = 1;

		if (ipAddressAttributes.length > minimumLength) {
			// determine 'weight':
			ipAddress.setWeight(NumberUtils.toDouble(ipAddressAttributes[minimumLength], 1));
		}

		minimumLength++;
		if (ipAddressAttributes.length > minimumLength) {
			// determine 'valid':
			if (Boolean.TRUE.toString().equals(ipAddressAttributes[minimumLength])
			|| Boolean.FALSE.toString().equals(ipAddressAttributes[minimumLength])) {
				ipAddress.setValid(Boolean.parseBoolean(ipAddressAttributes[minimumLength]));
			}

			// determine 'cluster':
			if (!Boolean.TRUE.toString().equals(ipAddressAttributes[minimumLength - 1]) && Boolean.FALSE.toString().equals(ipAddressAttributes[minimumLength - 1])) {
				ipAddress.setClusterName(ipAddressAttributes[minimumLength - 1]);
			}
		}

		minimumLength++;
		if (ipAddressAttributes.length > minimumLength) {
			// determine 'marked':
			if (Boolean.TRUE.equals(ipAddressAttributes[minimumLength]) || Boolean.FALSE.equals(ipAddressAttributes[minimumLength])) {
				ipAddress.setMarked(Boolean.parseBoolean(ipAddressAttributes[minimumLength]));

			}
		}

		return ipAddress;
	}

	public String toIPAddr() {
		return ip + ":" + port;
	}

	public String toJSON() {
		return JSON.toJSONString(this);
	}

	public static IpAddress fromJSON(String json) {
		IpAddress ip;

		try {
			ip = JSON.parseObject(json, IpAddress.class);
		}
		catch (Exception e) {
			ip = fromString(json);
		}

		if (ip == null) {
			throw new IllegalArgumentException("malfomed ip config: " + json);
		}

		if (ip.getWeight() > MAX_WEIGHT_VALUE) {
			ip.setWeight(MAX_WEIGHT_VALUE);
		}

		if (ip.getWeight() < MIN_POSITIVE_WEIGHT_VALUE && ip.getWeight() > MIN_WEIGHT_VALUE) {
			ip.setWeight(MIN_POSITIVE_WEIGHT_VALUE);
		}
		else if (ip.getWeight() < MIN_WEIGHT_VALUE) {
			ip.setWeight(0.0D);
		}
		return ip;
	}

	@JSONField(serialize = false)
	public String getDatumKey() {
		if (port > 0) {
			return ip + ":" + port + ":" + DistroMapper.LOCALHOST_SITE;
		}
		else {
			return ip + ":" + DistroMapper.LOCALHOST_SITE;
		}
	}

	@JSONField(serialize = false)
	public String getDefaultKey() {
		// TODO start here
		if (port > 0) {
			return ip + ":" + port + ":" + UtilsAndCommons.UNKNOWN_SITE;
		}
		else {
			return ip + ":" + UtilsAndCommons.UNKNOWN_SITE;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		IpAddress ipAddress = (IpAddress) o;
		return port == ipAddress.port && Objects.equals(ip, ipAddress.ip);
	}

	@Override
	public int hashCode() {
		return Objects.hash(ip);
	}

	public void setBeingChecked(boolean isBeingChecked) {
		HealthCheckStatus.get(this).isBeingChecked.set(isBeingChecked);
	}

	public boolean markChecking() {
		return HealthCheckStatus.get(this).isBeingChecked.compareAndSet(false, true);
	}

	@JSONField(serialize = false)
	public long getCheckRT() {

	}

	@Override
	public int compareTo(Object o) {
		return 0;
	}

	public static class InvalidType {
		public static final String HTTP_404 = "404";
		public static final String WEIGHT_0 = "weight_0";
		public static final String NORMAL_INVALID = "invalid";
		public static final String VALID = "valid";
	}
}

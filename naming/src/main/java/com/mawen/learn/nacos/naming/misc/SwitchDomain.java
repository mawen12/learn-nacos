package com.mawen.learn.nacos.naming.misc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSON;
import com.mawen.learn.nacos.naming.core.Domain;
import com.mawen.learn.nacos.naming.core.IpAddress;
import com.mawen.learn.nacos.naming.raft.RaftListener;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/23
 */
@Setter
@Getter
public class SwitchDomain implements Domain, RaftListener {

	private String name = "00-00---000-VIPSRV_SWITCH_DOMAIN-000---00-00";

	public List<String> masters;

	public Map<String, Integer> adWeightMap = new HashMap<>();

	public long defaultPushCacheMillis = TimeUnit.SECONDS.toMillis(10);

	private long clientBeatInterval = TimeUnit.SECONDS.toMillis(5);

	private long defaultCacheMillis = TimeUnit.SECONDS.toMillis(1);

	private float distroThreshold = 0.7f;

	public String token = UtilsAndCommons.SUPER_TOKEN;

	public Map<String, Long> cacheMillisMap = new HashMap<>();

	public Map<String, Long> pushCacheMillisMap = new HashMap<>();

	public boolean healthCheckEnabled = true;

	public boolean distroEnabled = true;

	public boolean enableStandalone = true;

	public int checkTimes = 3;

	public HttpHealthParams httpHealthParams = new HttpHealthParams();

	public TcpHealthParams tcpHealthParams = new TcpHealthParams();

	public MysqlHealthParams mysqlHealthParams = new MysqlHealthParams();

	private List<String> incrementalList = new ArrayList<>();

	private boolean allDomNameCache = true;

	public long serverStatusSynchronizationPeriodMillis = TimeUnit.SECONDS.toMillis(15);

	public long domStatusSynchronizationPeriodMillis = TimeUnit.SECONDS.toMillis(5);

	public boolean disableAddIP = false;

	public boolean enableCache = true;

	public boolean sendBeatOnly = false;

	public Map<String, Integer> limitedUrlMap = new HashMap<>();

	public long distroServerExpiredMillis = 30000;

	public String pushJavaVersion = "4.1.0";

	public String pushPythonVersion = "0.4.3";

	public String pushCVersion = "1.0.12";

	public String trafficSchedulingJavaVersion = "4.5.0";

	public String trafficSchedulingPythonVersion = "9999.0.0";

	public String trafficSchedulingCVersion = "1.0.5";

	public String trafficSchedulingTengineVersion = "2.0.0";

	public boolean enableAuthentication = false;

	public Set<String> healthCheckWhiteList = new HashSet<>();


	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {

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
		return masters;
	}

	@Override
	public void setOwners(List<String> owners) {
		this.masters = owners;
	}

	@Override
	public void init() {

	}

	@Override
	public void destroy() throws Exception {

	}

	@Override
	public List<IpAddress> allIPs() {
		return null;
	}

	@Override
	public List<IpAddress> srvIPs(String clientIP) {
		return null;
	}

	@Override
	public String toJSON() {
		return JSON.toJSONString(this);
	}

	@Override
	public void setProtectThreshold(float protectThreshold) {

	}

	@Override
	public float getProtectThreshold() {
		return 0;
	}

	@Override
	public void update(Domain domain) {

	}

	@Override
	public String getChecksum() {
		throw new NotImplementedException();
	}

	@Override
	public void recalculateChecksum() {
		throw new NotImplementedException();
	}

	@Override
	public boolean interests(String key) {
		return StringUtils.equals(key, UtilsAndCommons.DOMAINS_DATA_ID + "." + name);
	}

	@Override
	public boolean matchUnlistenKey(String key) {
		return StringUtils.equals(key, UtilsAndCommons.DOMAINS_DATA_ID + "." + name);
	}

	@Override
	public void onChange(String key, String value) throws Exception {
		SwitchDomain domain = JSON.parseObject(value, SwitchDomain.class);
		update(domain);
	}

	@Override
	public void onDelete(String key, String value) throws Exception {

	}


	public interface HealthParams {
		int getMax();

		int getMin();

		float getFactor();
	}

	@Getter
	@Setter
	public static class HttpHealthParams implements HealthParams {

		public static final int MIN_MAX = 3000;

		public static final int MIN_MIN = 500;

		private int max = 5000;
		private int min = 500;
		private float factor = 0.85F;
	}

	@Setter
	@Getter
	public static class MysqlHealthParams implements HealthParams {
		private int max = 3000;
		private int min = 2000;
		private float factor = 0.65F;
	}

	@Setter
	@Getter
	public static class TcpHealthParams implements HealthParams {
		private int max = 5000;
		private int min = 1000;
		private float factor = 0.75F;
	}
}

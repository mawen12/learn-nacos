package com.mawen.learn.nacos.naming.core;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.mawen.learn.nacos.naming.boot.RunningConfig;
import com.mawen.learn.nacos.naming.misc.NamingProxy;
import com.mawen.learn.nacos.naming.misc.ServerStatusSynchronizer;
import com.mawen.learn.nacos.naming.misc.Switch;
import com.mawen.learn.nacos.naming.misc.Synchronizer;
import com.mawen.learn.nacos.naming.misc.UtilsAndCommons;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/23
 */
@Slf4j
public class DistroMapper {

	public static final int STABLE_PERIOD = 60 * 1000;

	private static List<String> healthyList = new ArrayList<>();

	private static Map<String, List<Server>> distroConfig = new ConcurrentHashMap<>();

	private static Set<String> liveSites = new HashSet<>();

	private static String localhostIp;

	private static final String LOCALHOST_SITE = UtilsAndCommons.UNKNOWN_SITE;

	private static long LAST_HEALTH_SERVER_LIMITS = 0L;

	private static boolean AUTO_DISABLED_HEALTH_CHECK = false;

	private static Synchronizer synchronizer = new ServerStatusSynchronizer();

	static {
		try {
			localhostIp = InetAddress.getLocalHost().getHostAddress() + ":" + RunningConfig.getServerPort();
		}
		catch (UnknownHostException e) {
			throw new IllegalStateException("Unable to resolve current host IP");
		}

		init();

		UtilsAndCommons.SERVER_STATUS_EXECUTOR.schedule(new ServerStatusReporter(), 60000, TimeUnit.MILLISECONDS);
	}

	private static void init() {
		List<String> servers = NamingProxy.getServers();

		while (servers == null || servers.size() == 0) {
			log.warn("Server list is empty, sleep 3 seconds and try again");
			try {
				TimeUnit.SECONDS.sleep(3);
				servers = NamingProxy.getServers();
			}
			catch (InterruptedException e) {
				log.warn("Sleeping thread is interrupted, try again.");
			}
		}

		StringBuilder sb = new StringBuilder();

		for (String serverIp : servers) {
			String serverSite = UtilsAndCommons.UNKNOWN_SITE;
			// site:ip:lastReportTime:weight
			String serverConfig = serverSite + "#" + serverIp + "#" + System.currentTimeMillis() + "#" + 1 + "\r\n";
			sb.append(serverConfig);
		}

		onServerStatusUpdate(sb.toString(), false);
	}

	private static void onServerStatusUpdate(String configInfo, boolean isFromDiamond) {

		String[] configs = configInfo.split("\r\n");
		if (configs.length == 0) {
			return;
		}

		distroConfig.clear();
		List<String> newHealthyList = new ArrayList<>();

		for (String config : configs) {
			String[] params = config.split("#");
			if (params.length <= 3) {
				log.warn("received malformed distro map data: {}", config);
				continue;
			}

			Server server = new Server();
			server.site = params[0];
			server.site = params[1];
			server.lastRefTime = Long.parseLong(params[2]);
			Date date = new Date(Long.parseLong(params[2]));
			server.lastRefTimeStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
			server.weight = params.length == 4 ? Integer.parseInt(params[3]) : 1;
			server.alive = System.currentTimeMillis() - server.lastRefTime < Switch.getDistroServerExpiredMillis();

			List<Server> list = distroConfig.get(server.site);
			if (list == null) {
				list = new ArrayList<>();
				distroConfig.put(server.site, list);
			}

			list.add(server);
		}

		liveSites.addAll(distroConfig.keySet());

		List<Server> servers = distroConfig.get(LOCALHOST_SITE);
		if (CollectionUtils.isEmpty(servers)) {
			return;
		}

		List<String> allSiteSrvs = new ArrayList<>();
		for (Server server : servers) {
			server.adWeight = Switch.getAdWeight(server.ip) == null ? 0 : Switch.getAdWeight(server.ip);

			for (int i = 0; i < server.weight + server.adWeight; i++) {
				allSiteSrvs.add(server.ip);

				if (server.alive) {
					newHealthyList.add(server.ip);
				}
			}
		}

		Collections.sort(newHealthyList);
		float curRatio = (float) newHealthyList.size() / allSiteSrvs.size();

		if (AUTO_DISABLED_HEALTH_CHECK
				&& curRatio > Switch.getDistroThreshold()
				&& System.currentTimeMillis() - LAST_HEALTH_SERVER_LIMITS > STABLE_PERIOD) {
			log.info("distro threshold restored and stable now, enable health check. current ratio: {}", curRatio);

			Switch.setHealthCheckEnabled(true);

			AUTO_DISABLED_HEALTH_CHECK = false;
		}

		if (!CollectionUtils.isEqualCollection(healthyList, newHealthyList)) {
			if (Switch.isHealthCheckEnabled()) {
				log.info("healthy server list changed, disable health check for {}ms from now on, healthList: {}, newHealthyList: {}",
						STABLE_PERIOD, healthyList, newHealthyList);

				Switch.setHealthCheckEnabled(false);

				AUTO_DISABLED_HEALTH_CHECK = true;

				LAST_HEALTH_SERVER_LIMITS = System.currentTimeMillis();
			}

			healthyList = newHealthyList;
		}
	}

	public static synchronized void onReceiveServerStatus(String configInfo) {
		String[] configs = configInfo.split("\r\n");
		if (configs.length == 0) {
			return;
		}

		distroConfig.clear();
		List<String> newHealthyList = new ArrayList<>();

		for (String config : configs) {
			String[] params = config.split("#");
			if (params.length <= 3) {
				log.warn("received malformed distro map data: {}", config);
				continue;
			}

			Server server = new Server();
			server.site = params[0];
			server.site = params[1];
			server.lastRefTime = Long.parseLong(params[2]);
			Date date = new Date(Long.parseLong(params[2]));
			server.lastRefTimeStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
			server.weight = params.length == 4 ? Integer.parseInt(params[3]) : 1;
			server.alive = System.currentTimeMillis() - server.lastRefTime < Switch.getDistroServerExpiredMillis();

			List<Server> list = distroConfig.get(server.site);
			if (list == null) {
				list = new ArrayList<>();
				distroConfig.put(server.site, list);
			}

			list.add(server);
		}

		liveSites.addAll(distroConfig.keySet());

		List<Server> servers = distroConfig.get(LOCALHOST_SITE);
		if (CollectionUtils.isEmpty(servers)) {
			return;
		}

		List<String> allSiteSrvs = new ArrayList<>();
		for (Server server : servers) {
			server.adWeight = Switch.getAdWeight(server.ip) == null ? 0 : Switch.getAdWeight(server.ip);

			for (int i = 0; i < server.weight + server.adWeight; i++) {
				allSiteSrvs.add(server.ip);

				if (server.alive) {
					newHealthyList.add(server.ip);
				}
			}
		}

		Collections.sort(newHealthyList);
		float curRatio = (float) newHealthyList.size() / allSiteSrvs.size();

		if (AUTO_DISABLED_HEALTH_CHECK
				&& curRatio > Switch.getDistroThreshold()
				&& System.currentTimeMillis() - LAST_HEALTH_SERVER_LIMITS > STABLE_PERIOD) {
			log.info("distro threshold restored and stable now, enable health check. current ratio: {}", curRatio);

			Switch.setHealthCheckEnabled(true);

			AUTO_DISABLED_HEALTH_CHECK = false;
		}

		if (!CollectionUtils.isEqualCollection(healthyList, newHealthyList)) {
			if (Switch.isHealthCheckEnabled()) {
				log.info("healthy server list changed, disable health check for {}ms from now on, healthList: {}, newHealthyList: {}",
						STABLE_PERIOD, healthyList, newHealthyList);

				Switch.setHealthCheckEnabled(false);

				AUTO_DISABLED_HEALTH_CHECK = true;

				LAST_HEALTH_SERVER_LIMITS = System.currentTimeMillis();
			}

			healthyList = newHealthyList;
		}
	}

	public static boolean responsible(String dom) {
		if (!Switch.isDistroEnabled()) {

		}
	}

	public static class Server {
		public String site = UtilsAndCommons.UNKNOWN_SITE;
		public String ip;
		public int weight = 1;
		public int adWeight;
		public boolean alive = false;
		public long lastRefTime = 0L;
		public String lastRefTimeStr;
	}

	private static class ServerStatusReporter implements Runnable {

		@Override
		public void run() {
			for (String key : distroConfig.keySet()) {
				for (Server server : distroConfig.get(key)) {
					server.alive = System.currentTimeMillis() - server.lastRefTime < Switch
				}
			}
		}
	}
}

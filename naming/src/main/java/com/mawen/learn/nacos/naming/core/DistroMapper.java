package com.mawen.learn.nacos.naming.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.mawen.learn.nacos.naming.misc.UtilsAndCommons;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/23
 */
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

	public static class Server {
		public String site = UtilsAndCommons.UNKNOWN_SITE;
		public String ip;
		public int weight = 1;
		public int adWeight;
		public boolean alive = false;
		public long lastRefTime = 0L;
		public String lastRefTimeStr;
	}

	private static class ServerStatusSynchronizer implements Runnable {

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

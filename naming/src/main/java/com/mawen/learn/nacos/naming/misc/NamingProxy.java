package com.mawen.learn.nacos.naming.misc;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.mawen.learn.nacos.common.util.IoUtils;
import com.mawen.learn.nacos.common.util.SystemUtil;
import com.mawen.learn.nacos.naming.boot.RunningConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/24
 */
@Slf4j
public class NamingProxy {

	private static volatile List<String> servers;

	private static List<String> serverlistFromConfig;

	private static List<String> lastServers = new ArrayList<>();

	private static Map<String, List<String>> serverListMap = new ConcurrentHashMap<>();

	private static long lastSrvRefTime = 0L;

	private static long lastSrvSiteRefreshTime = 0L;

	private static long VIP_SRV_REF_INTER_MILLIS = TimeUnit.SECONDS.toMillis(30);

	private static final long VIP_SRV_SITE_REF_INTER_MILLIS = TimeUnit.HOURS.toMillis(1);

	private static String jmenv;

	public static String getJmenv() {
		jmenv = SystemUtil.getSystemEnv("nacos_jmenv_domain");

		if (StringUtils.isEmpty(jmenv)) {
			jmenv = System.getProperty("com.mawen.learn.nacos.jmenv", "jmenv.tbsite.net");
		}

		if (StringUtils.isEmpty(jmenv)) {
			jmenv = "jmenv.tbsite.net";
		}

		return jmenv;
	}

	private static void refreshSrvSiteIfNeed() {
		refreshSrvIfNeed();
		try {
			if (System.currentTimeMillis() - lastSrvSiteRefreshTime > VIP_SRV_SITE_REF_INTER_MILLIS ||
					!CollectionUtils.isEqualCollection(servers, lastServers)) {
				if (!CollectionUtils.isEqualCollection(servers, lastServers)) {
					log.info("server list is changed, old: {}, new: {}", lastServers, servers);
				}

				lastServers = servers;
			}
		}
		catch (Exception e) {
			log.warn("fail to query server site", e);
		}
	}

	public static List<String> getServers() {
		refreshSrvIfNeed();
		return servers;
	}

	public static void refreshSrvIfNeed() {
		refreshSrvIfNeed(StringUtils.EMPTY);
	}

	public static void refreshSrvIfNeed(String env) {
		try {
			if (System.currentTimeMillis() - lastSrvRefTime < VIP_SRV_REF_INTER_MILLIS) {
				return;
			}

			if (UtilsAndCommons.STANDALONE_MODE) {
				servers = new ArrayList<>();
				servers.add(InetAddress.getLocalHost().getHostAddress() + ":" + RunningConfig.getServerPort());
				return;
			}

			List<String> serverList = refreshServerListFromDisk();

			List<String> list = new ArrayList<>();
			if (!CollectionUtils.isEmpty(serverList)) {
				serverlistFromConfig = serverList;
				if (list.isEmpty()) {
					log.warn("Can not acquire server list");
				}
			}

			if (!StringUtils.isEmpty(env)) {
				serverListMap.put(env, list);
			}
			else {
				if (!CollectionUtils.isEqualCollection(serverlistFromConfig, list) && CollectionUtils.isNotEmpty(serverlistFromConfig)) {
					log.info("server list is not the same between AS and config file, use config file.");
					servers = serverlistFromConfig;
				}
				else {
					servers = list;
				}
			}

			if (RunningConfig.getServerPort() > 0) {
				lastSrvRefTime = System.currentTimeMillis();
			}
		}
		catch (Exception e) {
			log.warn("failed to update server list", e);
			List<String> serverList = refreshServerListFromDisk();

			if (CollectionUtils.isNotEmpty(serverList)) {
				serverlistFromConfig = serverList;
			}

			if (CollectionUtils.isNotEmpty(serverlistFromConfig)) {
				servers = serverlistFromConfig;
			}
		}
	}

	public static List<String> refreshServerListFromDisk() {

		List<String> result = new ArrayList<>();

		try {
			result = IoUtils.readLines(new InputStreamReader(new FileInputStream(UtilsAndCommons.getConfFile()), "UTF-8"));
		}
		catch (Exception e) {
			log.warn("failed to get config: {}", UtilsAndCommons.getConfFile(), e);
		}

		log.debug(result.toString());

		// use system env
		if (CollectionUtils.isEmpty(result)) {
			result = SystemUtil.getIpsBySystemEnv(UtilsAndCommons.SELF_SERVICE_CLUSTER_ENV);
			log.debug(result.toString());
		}

		if (!result.isEmpty() && !result.get(0).contains(UtilsAndCommons.CLUSTER_CONF_IP_SPLITTER)) {
			for (int i = 0; i < result.size(); i++) {
				result.set(i, result.get(i) + UtilsAndCommons.CLUSTER_CONF_IP_SPLITTER + RunningConfig.getServerPort());
			}
		}

		return result;
	}

	public static ConcurrentHashMap<String, List<String>> getSameSiteServers() {
		refreshSrvIfNeed();
		List<String> snapshot = servers;
		ConcurrentHashMap<String, List<String>> servers = new ConcurrentHashMap<>(2);
		servers.put("sameSite", snapshot);
		servers.put("otherSite", new ArrayList<>());

		log.debug("sameSiteServers: {}", servers);
		return servers;
	}

	public static String reqAPI(String api, Map<String, String> params, String curServer, boolean isPost) {
		try {
			List<String> headers = Arrays.asList("Client-Version", UtilsAndCommons.SERVER_VERSION,
					"Accept-Encoding", "gzip,deflate,sdch",
					"Connection", "Keep-Alive",
					"Content-Encoding", "gzip");

			HttpClient.HttpResult result;

			if (!curServer.contains(UtilsAndCommons.CLUSTER_CONF_IP_SPLITTER)) {
				curServer = curServer + UtilsAndCommons.CLUSTER_CONF_IP_SPLITTER + RunningConfig.getServerPort();
			}

			if (isPost) {
				result = HttpClient.httpPost("http://" + curServer + RunningConfig.getContextPath() + UtilsAndCommons.NACOS_NAMING_CONTEXT + "/api/" + api, headers, params);
			}
			else {
				result = HttpClient.httpGet("http://" + curServer + RunningConfig.getContextPath() + UtilsAndCommons.NACOS_NAMING_CONTEXT + "/api" + api, headers, params);
			}

			if (HttpURLConnection.HTTP_OK == result.code) {
				return result.content;
			}

			if (HttpURLConnection.HTTP_NOT_MODIFIED == result.code) {
				return StringUtils.EMPTY;
			}

			throw new IOException("failed to req API: http://" + curServer + RunningConfig.getContextPath() +
					UtilsAndCommons.NACOS_NAMING_CONTEXT + "/api/" + api
					+ ". code:" + result.code
					+ " msg: " + result.content);
		}
		catch (Exception e) {
			log.warn(e.toString());
		}
		return StringUtils.EMPTY;
	}

	public static String getEnv() {
		try {
			String url = "http://" + getJmenv() + ":8080" + "/env";

			List<String> headers = Arrays.asList("Client-Version", UtilsAndCommons.SERVER_VERSION,
					"Accept-Encoding", "gzip,deflate,sdch",
					"Connection", "Keep-Alive");

			HttpClient.HttpResult result = HttpClient.httpGet(url, headers, null);
			if (HttpURLConnection.HTTP_OK != result.code) {
				throw new IOException("Error while requesting: " + url + ". Server Returned: " + result.code);
			}

			String content = result.content;
			return content.trim();
		}
		catch (Exception e) {
			log.warn("failed to get env", e);
		}

		return "sh";
	}
}

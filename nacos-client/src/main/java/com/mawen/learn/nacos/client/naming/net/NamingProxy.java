package com.mawen.learn.nacos.client.naming.net;

import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSON;
import com.mawen.learn.nacos.api.exception.NacosException;
import com.mawen.learn.nacos.api.naming.pojo.Instance;
import com.mawen.learn.nacos.client.naming.utils.CollectionUtils;
import com.mawen.learn.nacos.client.naming.utils.UtilAndComs;
import com.mawen.learn.nacos.common.util.IoUtils;
import com.mawen.learn.nacos.common.util.UuidUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/19
 */
public class NamingProxy {

	private static final Logger log = LoggerFactory.getLogger(NamingProxy.class);

	private String namespace;

	private String endpoint;

	private String nacosDomain;

	private List<String> serverList;

	private List<String> serversFromEndpoint = new ArrayList<>();

	private long lastSrvRefTime = 0L;

	private long vipSrvRefInterMillis = TimeUnit.SECONDS.toMillis(30);

	private ScheduledExecutorService executorService;

	public NamingProxy(String namespace, String endpoint, String serverList) {
		this.namespace = namespace;
		this.endpoint = endpoint;
		if (StringUtils.isNotEmpty(serverList)) {
			this.serverList = Arrays.asList(serverList.split(","));
			if (this.serverList.size() == 1) {
				this.nacosDomain = serverList;
			}
		}

		this.executorService = new ScheduledThreadPoolExecutor(1, r -> {
			Thread t = new Thread(r);
			t.setName("com.mawen.vipserver.serverlist.updater");
			t.setDaemon(true);
			return t;
		});

		this.executorService.scheduleWithFixedDelay(() -> refreshSrvIfNeed(), 0, vipSrvRefInterMillis, TimeUnit.MILLISECONDS);

		refreshSrvIfNeed();
	}

	public List<String> getServerListFromEndpoint() {

		try {
			String urlString = "http://" + endpoint + "/vipserver/serverlist";

			List<String> headers = Arrays.asList("Client-Version", UtilAndComs.VERSION,
					"Accept-Encoding", "gzip,deflate,sdch",
					"Connection", "Keep-Alive",
					"RequestId", UuidUtil.generateUuid());

			HttpClient.HttpResult result = HttpClient.httpGet(urlString, headers, null, UtilAndComs.ENCODING);
			if (HttpURLConnection.HTTP_OK != result.code) {
				throw new IOException("Error while requesting: " + urlString + ". Server returned: " + result.code);
			}

			String content = result.content;
			List<String> list = new ArrayList<>();
			for (String line : IoUtils.readLines(new StringReader(content))) {
				if (!line.trim().isEmpty()) {
					list.add(line.trim());
				}
			}

			return list;
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public void registerService(String serviceName, Instance instance) throws NacosException {
		final Map<String, String> params = new HashMap<>(8);
		params.put("tenant", namespace);
		params.put("ip", instance.getIp());
		params.put("port", String.valueOf(instance.getPort()));
		params.put("weight", String.valueOf(instance.getWeight()));
		params.put("healthy", String.valueOf(instance.isHealthy()));
		params.put("metadata", JSON.toJSONString(instance.getMetadata()));
		if (instance.getService() == null) {
			params.put("serviceName", serviceName);
		}
		else {
			params.put("service", JSON.toJSONString(instance.getService()));
		}
		params.put("cluster", JSON.toJSONString(instance.getCluster()));

		reqAPI(UtilAndComs.NACOS_URL_INSTANCE, params, "PUT");
	}

	public void deregisterService(String serviceName, String ip, int port, String cluster) throws NacosException {
		final Map<String, String> params = new HashMap<>(8);
		params.put("tenant", namespace);
		params.put("ip", ip);
		params.put("port", String.valueOf(port));
		params.put("serviceName", serviceName);
		params.put("cluster", cluster);

		reqAPI(UtilAndComs.NACOS_URL_INSTANCE, params, "DELETE");
	}

	public String reqAPI(String api, Map<String, String> params) {

		List<String> snapshot = serversFromEndpoint;
		if (!CollectionUtils.isEmpty(serverList)) {
			snapshot = serverList;
		}

		return reqAPI(api, params, snapshot);
	}

	public String reqAPI(String api, Map<String, String> params, String method) {

		List<String> snapshot = serversFromEndpoint;
		if (!CollectionUtils.isEmpty(serverList)) {
			snapshot = serverList;
		}

		return reqAPI(api, params, snapshot, method);
	}

	public String reqAPI(String api, Map<String, String> params, List<String> servers) {
		return reqAPI(api, params, servers, "GET");
	}

	public String reqAPI(String api, Map<String, String> params, List<String> servers, String method) {

		if (CollectionUtils.isEmpty(servers) && StringUtils.isEmpty(nacosDomain)) {
			throw new IllegalArgumentException("no server available");
		}

		if (servers != null && !servers.isEmpty()) {
			Random random = new Random(System.currentTimeMillis());
			int index = random.nextInt(servers.size());

			for (int i = 0; i < servers.size(); i++) {
				String server = servers.get(index);
				try {
					return callServer(api, params, server, method);
				}
				catch (Exception e) {
					log.error("NA req api: {} failed, server({})", api, server, e);
				}

				index = (index + 1) % servers.size();
			}

			throw new IllegalStateException("failed to req API: " + api + " after all servers(" + servers + ") tried");
		}

		for (int i = 0; i < UtilAndComs.REQUEST_DOMAIN_RETRY_COUNT; i++) {
			try {
				return callServer(api, params, nacosDomain);
			}
			catch (Exception e) {
				log.error("failed to req API:/api/{} after all servers({}) tried", api, servers);
			}
		}

		throw new IllegalStateException("failed to req API:/api/" + api + " after all servers(" + servers + ") tried");
	}

	public String callServer(String api, Map<String, String> params, String curServer) throws NacosException {
		return callServer(api, params, curServer, "GET");
	}

	public String callServer(String api, Map<String, String> params, String curServer, String method) throws NacosException {
		List<String> headers = Arrays.asList("Client-Version", UtilAndComs.VERSION,
				"Accept-Encoding", "gzip,deflate,sdch",
				"Connection", "Keep-Alive",
				"RequestId", UuidUtil.generateUuid());

		String url = HttpClient.getPrefix() + curServer + api;

		HttpClient.HttpResult result = HttpClient.request(url, headers, params, UtilAndComs.ENCODING, method);

		if (HttpURLConnection.HTTP_OK != result.code) {
			return result.content;
		}

		if (HttpURLConnection.HTTP_NOT_MODIFIED == result.code) {
			return StringUtils.EMPTY;
		}

		log.error("CALL_SERVER failed to req API: {}. code: {} msg: {}", url, result.code, result.content);

		throw new NacosException(NacosException.SERVER_ERROR, "failed to req API: " + url + ". code: " + result.code + " msg: " + result.content);
	}

	private void refreshSrvIfNeed() {

		try {
			if (!CollectionUtils.isEmpty(serverList)) {
				log.info("server list provided by user: {}", serverList);
				return;
			}

			if (System.currentTimeMillis() - lastSrvRefTime < vipSrvRefInterMillis) {
				return;
			}

			List<String> list = getServerListFromEndpoint();

			if (list.isEmpty()) {
				throw new Exception("Can not acquire vipserver list");
			}

			if (!CollectionUtils.isEqualCollection(list, serversFromEndpoint)) {
				log.info("SERVER-LIST server list is updated: {}", list);
			}

			serversFromEndpoint = list;
			lastSrvRefTime = System.currentTimeMillis();
		}
		catch (Throwable e) {
			log.warn("failed to update server list", e);
		}
	}

}

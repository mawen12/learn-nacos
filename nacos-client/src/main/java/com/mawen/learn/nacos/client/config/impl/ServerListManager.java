package com.mawen.learn.nacos.client.config.impl;

import java.io.IOException;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.mawen.learn.nacos.api.PropertyKeyConst;
import com.mawen.learn.nacos.api.exception.NacosException;
import com.mawen.learn.nacos.client.config.utils.EnvUtil;
import com.mawen.learn.nacos.client.utils.ParamUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/21
 */
public class ServerListManager {

	private static final Logger log = LoggerFactory.getLogger(ServerListManager.class);

	public static final String DEFAULT_NAME = "default";

	public static final String CUSTOM_NAME = "custom";

	public static final String FIXED_NAME = "fixed";

	public static final int TIMEOUT = 5000;

	private String name;

	private String namespace;

	private String tenant;

	private int initServerListRetryTimes = 5;

	private final boolean isFixed;

	private boolean isStarted;

	private String endpoint;

	private int endpointPort = 8080;

	private String contentPath = ParamUtil.getDefaultContextPath();

	private String serverListName = ParamUtil.getDefaultNodesPath();

	private volatile List<String> serverUrls = new ArrayList<>();

	private volatile String currentServerAddr;

	private String serverPort = ParamUtil.getDefaultServerPort();

	private String addressServerUrl;

	public ServerListManager() {
		this.isFixed = false;
		this.isStarted = false;
		this.name = DEFAULT_NAME;
	}

	public ServerListManager(List<String> fixed) {

	}

	public ServerListManager(List<String> fixed, String namespace) {
		this.isFixed = true;
		this.isStarted = true;
		List<String> serverAddrs = new ArrayList<>();
		for (String serverAddr : fixed) {
			String[] serverAddrArr = serverAddr.split(":");
			if (serverAddrArr.length == 1) {
				serverAddrs.add(serverAddrArr[0] + ":" + ParamUtil.getDefaultServerPort());
			}
			else {
				serverAddrs.add(serverAddr);
			}
		}
		this.serverUrls = new ArrayList<>(serverAddrs);
		if (StringUtils.isBlank(namespace)) {
			name = FIXED_NAME + "-" + getFixedNameSuffix(serverAddrs.toArray(new String[serverAddrs.size()]));
		}
		else {
			this.namespace = namespace;
			this.name = FIXED_NAME + "-" + getFixedNameSuffix(serverAddrs.toArray(new String[serverAddrs.size()])) + "-" + namespace;
		}
	}

	public ServerListManager(String host, int port) {
		this.isFixed = false;
		this.isStarted = false;
		this.name = CUSTOM_NAME + "-" + host + "-" + port;
		this.addressServerUrl = String.format("http://%s:%d/%s/%s", host, port, contentPath, serverListName);
	}

	public ServerListManager(String endpoint) throws NacosException {
		this(endpoint, null);
	}

	public ServerListManager(String endpoint, String namespace) throws NacosException {
		this.isFixed = false;
		this.isStarted = false;
		if (StringUtils.isBlank(endpoint)) {
			throw new NacosException(NacosException.CLIENT_INVALID_PARAM, "endpoint is blank");
		}
		if (StringUtils.isBlank(namespace)) {
			this.name = endpoint;
			this.addressServerUrl = String.format("http://%s:%d/%s/%s", endpoint, endpointPort, contentPath, serverListName);
		}
		else {
			if (StringUtils.isBlank(endpoint)) {
				throw new NacosException(NacosException.CLIENT_INVALID_PARAM, "endpoint is blank");
			}

			this.name = endpoint + "-" + namespace;
			this.namespace = namespace;
			this.tenant = namespace;
			this.addressServerUrl = String.format("http://%s:%d/%s/%s?namespace=%s", endpoint, endpointPort, contentPath, serverListName, namespace);
		}
	}

	public ServerListManager(Properties properties) throws NacosException {
		this.isStarted = false;
		String serverAddrsStr = properties.getProperty(PropertyKeyConst.SERVER_ADDR);
		String namespace = properties.getProperty(PropertyKeyConst.NAMESPACE);
		initParam(properties);
		if (StringUtils.isNotEmpty(serverAddrsStr)) {
			this.isFixed = true;
			List<String> serverAddrs = new ArrayList<>();
			String[] serverAddrsArr = serverAddrsStr.split(",");
			for (String serverAddr : serverAddrsArr) {
				String[] serverAddrArr = serverAddr.split(":");
				if (serverAddrArr.length == 1) {
					serverAddrs.add(serverAddrArr[0] + ":" + ParamUtil.getDefaultServerPort());
				}
				else {
					serverAddrs.add(serverAddr);
				}
			}
			this.serverUrls = serverAddrs;
			if (StringUtils.isBlank(namespace)) {
				this.name = FIXED_NAME + "-" + getFixedNameSuffix(serverUrls.toArray(new String[serverUrls.size()]));
			}
			else {
				this.namespace = namespace;
				this.tenant = namespace;
				this.name = FIXED_NAME + "-" + getFixedNameSuffix(serverAddrs.toArray(new String[serverUrls.size()])) + "-" + namespace;
			}
		}
		else {
			if (StringUtils.isBlank(endpoint)) {
				throw new NacosException(NacosException.CLIENT_INVALID_PARAM, "endpoint is blank");
			}

			this.isFixed = false;
			if (StringUtils.isBlank(namespace)) {
				this.name = endpoint;
				this.addressServerUrl = String.format("http://%s:%d/%s/%s", endpoint, endpointPort, contentPath, serverListName);
			}
			else {
				this.namespace = namespace;
				this.tenant = namespace;
				this.name = endpoint + "-" + namespace;
				this.addressServerUrl = String.format("http://%s:%d/%s/%s?namespace=%s", endpoint, endpointPort, contentPath, serverListName, namespace);
			}
		}
	}

	public synchronized void start() throws NacosException {
		if (isFixed || isStarted) {
			return;
		}

		GetServerListTask task = new GetServerListTask(addressServerUrl);
		for (int i = 0; i < initServerListRetryTimes && serverUrls.isEmpty(); i++) {
			task.run();
			try {
				this.wait((i + 1) * 1000L);
			}
			catch (InterruptedException e) {
				log.warn("get serverlist fail, url: {}", addressServerUrl);
			}
		}

		if (serverUrls.isEmpty()) {
			log.error("NACOS-0008 环境问题 fail to get NACOS-server serverlist! env: {}, not connect url: {}", name, addressServerUrl);
			log.error("NACOS-XXXX {} [init-serverlist] fail to get NACOS-server serverlist!", name);
			throw new NacosException(NacosException.SERVER_ERROR, "fail to get NACOS-server serverlist! env: " + name + ", not connect url: " + addressServerUrl);
		}

		TimerService.scheduleWithFixedDelay(task, 0L, 30L, TimeUnit.SECONDS);
		this.isStarted = true;
	}

	public String getName() {
		return name;
	}

	public String getNamespace() {
		return namespace;
	}

	public String getTenant() {
		return tenant;
	}

	public String getContentPath() {
		return contentPath;
	}

	public String getCurrentServerAddr() {
		if (StringUtils.isBlank(currentServerAddr)) {
			currentServerAddr = iterator().next();
		}
		return currentServerAddr;
	}

	public void refreshCurrentServerAddr() {
		this.currentServerAddr = iterator().next();
	}

	public boolean contain(String ip) {
		return serverUrls.contains(ip);
	}

	public String getUrlString() {
		return serverUrls.toString();
	}

	public String getFixedNameSuffix(String... serverIps) {
		StringBuilder sb = new StringBuilder();
		String split = "";
		for (String serverIp : serverIps) {
			sb.append(split);
			sb.append(serverIp);
			split = "-";
		}
		return sb.toString();
	}

	Iterator<String> iterator() {
		if (serverUrls.isEmpty()) {
			log.error("NACOS-XXX {} [iterator-serverlist] No server address defined!", name);
		}
		return new ServerAddressIterator(serverUrls);
	}

	private void initParam(Properties properties) {
		String endpointImp = properties.getProperty(PropertyKeyConst.ENDPOINT);
		if (!StringUtils.isBlank(endpointImp)) {
			this.endpoint = endpointImp;
		}
		String contentPathImp = properties.getProperty(PropertyKeyConst.CONTEXT_PATH);
		if (!StringUtils.isBlank(contentPathImp)) {
			this.contentPath = contentPathImp;
		}
		String serverListNameTmp = properties.getProperty(PropertyKeyConst.CLUSTER_NAME);
		if (!StringUtils.isBlank(serverListNameTmp)) {
			this.serverListName = serverListNameTmp;
		}
	}

	private List<String> getApacheServerList(String url, String name) {
		try {
			HttpSimpleClient.HttpResult httpResult = HttpSimpleClient.httpGet(url, null, null, null, 3000);

			if (HttpURLConnection.HTTP_OK == httpResult.code) {
				if (DEFAULT_NAME.equals(name)) {
					EnvUtil.setSelfEnv(httpResult.headers);
				}

				List<String> lines = IOUtils.readLines(new StringReader(httpResult.content));
				List<String> result = new ArrayList<>(lines.size());
				for (String serverAddr : lines) {
					if (null == serverAddr || serverAddr.trim().isEmpty()) {
						continue;
					}
					else {
						String[] ipPort = serverPort.trim().split(":");
						String ip = ipPort[0].trim();
						if (ipPort.length == 1) {
							result.add(ip + ":" + ParamUtil.getDefaultServerPort());
						}
						else {
							result.add(serverAddr);
						}
					}
				}
				return result;
			}
			else {
				log.error("NACOS-XXXX [check-serverlist] error, code = {}, serverlist = {}.", httpResult.code, addressServerUrl);
				return null;
			}
		}
		catch (IOException e) {
			log.error("NACOS-0001 环境问题", e);
			log.error("NACOS-XXXX [check-serverlist] error.", e);
			return null;
		}
	}

	private void updateIfChanged(List<String> newList) {
		if (null == newList || newList.isEmpty()) {
			log.warn("NACOS-0001 环境问题 [update-serverlist] current serverlist from address server is empty!!!");
			log.warn("{} [update-serverlist] current serverlist from address server is empty!!!", name);
			return;
		}

		if (newList.equals(serverUrls)) {
			return;
		}

		serverUrls = new ArrayList<>(newList);
		currentServerAddr = iterator().next();

		EventDispatcher.fireEvent(new EventDispatcher.ServerlistChangeEvent());
		log.info("{} [update-serverlist] serverlist updated to {}", name, serverUrls);
	}

	@Override
	public String toString() {
		return "ServerManager-" + name + "-" + getUrlString();
	}

	class GetServerListTask implements Runnable {
		private final String url;

		public GetServerListTask(String url) {
			this.url = url;
		}

		@Override
		public void run() {
			try {
				updateIfChanged(getApacheServerList(url, name));
			}
			catch (Exception e) {
				log.error("NACOS-XXXX {} [update-serverlist] failed to update serverlist from addres server!", name, e);
			}
		}
	}

	static class ServerAddressIterator implements Iterator<String> {

		private final List<RandomizedServerAddress> sorted;

		private final Iterator<RandomizedServerAddress> iter;

		public ServerAddressIterator(List<String> source) {
			this.sorted = new ArrayList<>();
			for (String address : source) {
				sorted.add(new RandomizedServerAddress(address));
			}
			Collections.sort(sorted);
			this.iter = sorted.iterator();
		}

		@Override
		public boolean hasNext() {
			return iter.hasNext();
		}

		@Override
		public String next() {
			return iter.next().serverIp;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	static class RandomizedServerAddress implements Comparable<RandomizedServerAddress> {

		static Random random = new Random();

		private String serverIp;

		private int priority;

		private int seed;

		public RandomizedServerAddress(String ip) {
			try {
				this.serverIp = ip;
				this.seed = random.nextInt(Integer.MAX_VALUE);
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public int compareTo(RandomizedServerAddress o) {
			if (this.priority != o.priority) {
				return o.priority - this.priority;
			}
			else {
				return this.seed - o.seed;
			}
		}
	}

}

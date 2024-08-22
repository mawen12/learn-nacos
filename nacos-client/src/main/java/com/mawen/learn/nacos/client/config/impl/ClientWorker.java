package com.mawen.learn.nacos.client.config.impl;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.mawen.learn.nacos.api.config.listener.Listener;
import com.mawen.learn.nacos.api.exception.NacosException;
import com.mawen.learn.nacos.client.config.common.Constants;
import com.mawen.learn.nacos.client.config.common.GroupKey;
import com.mawen.learn.nacos.client.config.filter.impl.ConfigFilterChainManager;
import com.mawen.learn.nacos.client.config.utils.ContentUtils;
import com.mawen.learn.nacos.client.config.utils.MD5;
import com.mawen.learn.nacos.client.config.utils.TenantUtil;
import com.mawen.learn.nacos.client.utils.ParamUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/21
 */
@Slf4j
public class ClientWorker {

	private final ScheduledExecutorService executor;

	private final ExecutorService executorService;

	private AtomicReference<Map<String, CacheData>> cacheMap = new AtomicReference<>();

	private ServerHttpAgent agent;

	private ConfigFilterChainManager configFilterChainManager;

	private boolean isHealthServer = true;

	private double currentLongingTaskCount = 0;

	public ClientWorker(final ServerHttpAgent agent, final ConfigFilterChainManager configFilterChainManager) {
		this.agent = agent;
		this.configFilterChainManager = configFilterChainManager;
		this.executor = Executors.newScheduledThreadPool(1, r -> {
			Thread t = new Thread(r);
			t.setName("com.mawen.learn.nacos.client.Worker." + agent.getName());
			t.setDaemon(true);
			return t;
		});
		this.executorService = Executors.newCachedThreadPool(r -> {
			Thread t = new Thread(r);
			t.setName("com.mawen.learn.nacos.client.Worker.longPulling." + agent.getName());
			t.setDaemon(true);
			return t;
		});

		this.executor.scheduleWithFixedDelay(() -> {
			try {
				checkConfigInfo();
			}
			catch (Throwable e) {
				log.error("[sub-check] rotate check error", e);
			}
		}, 1L, 10L, TimeUnit.MILLISECONDS);
	}

	public boolean isHealthServer() {
		return isHealthServer;
	}

	private void setHealthServer(boolean isHealthServer) {
		this.isHealthServer = isHealthServer;
	}

	public void addListener(String dataId, String group, List<? extends Listener> listeners) {
		group = null2DefaultGroup(group);
		CacheData cache = addCacheDataIfAbsent(dataId, group);
		for (Listener listener : listeners) {
			cache.addListener(listener);
		}
	}

	public void removeListener(String dataId, String group, Listener listener) {
		group = null2DefaultGroup(group);
		CacheData cache = getCache(dataId, group);
		if (null != cache) {
			cache.removeListener(listener);
			if (cache.getListeners().isEmpty()) {
				removeCache(dataId, group);
			}
		}
	}

	public void addTenantListeners(String dataId, String group, List<? extends Listener> listeners) {
		group = null2DefaultGroup(group);
		String tenant = agent.getTenant();
		CacheData cache = addCacheDataIfAbsent(dataId, group, tenant);
		for (Listener listener : listeners) {
			cache.addListener(listener);
		}
	}

	public void removeTenantListener(String dataId, String group, Listener listener) {
		group = null2DefaultGroup(group);
		String tenant = agent.getTenant();
		CacheData cache = getCache(dataId, group, tenant);
		if (null != cache) {
			cache.removeListener(listener);
			if (cache.getListeners().isEmpty()) {
				removeCache(dataId, group, tenant);
			}
		}
	}

	void removeCache(String dataId, String group) {
		String groupKey = GroupKey.getKey(dataId, group);
		synchronized (cacheMap) {
			Map<String, CacheData> copy = new HashMap<>(cacheMap.get());
			copy.remove(groupKey);
			cacheMap.set(copy);
		}
		log.info("[unsubscribe] {}", groupKey);
	}

	void removeCache(String dataId, String group, String tenant) {
		String groupKey = GroupKey.getKey(dataId, group, tenant);
		synchronized (cacheMap) {
			Map<String, CacheData> copy = new HashMap<>(cacheMap.get());
			copy.remove(groupKey);
			cacheMap.set(copy);
		}
		log.info("[unsubscribe] {}", groupKey);
	}

	public CacheData addCacheDataIfAbsent(String dataId, String group) {
		CacheData cache = getCache(dataId, group);
		if (null != cache) {
			return cache;
		}

		String key = GroupKey.getKey(dataId, group);
		cache = new CacheData(configFilterChainManager, agent.getName(), dataId, group);

		synchronized (cacheMap) {
			CacheData cacheFromMap = getCache(dataId, group);
			if (null != cacheFromMap) {
				cache = cacheFromMap;
				cache.setInitializing(true);
			}
			else {
				int taskId = cacheMap.get().size() / (int) ParamUtil.getPerTaskConfigSize();
				cache.setTaskId(taskId);
			}

			Map<String, CacheData> copy = new HashMap<>(cacheMap.get());
			copy.put(key, cache);
			cacheMap.set(copy);
		}

		log.info("[subscribe] {}", key);

		return cache;
	}

	public CacheData addCacheDataIfAbsent(String dataId, String group, String tenant) {
		CacheData cache = getCache(dataId, group, tenant);
		if (null != cache) {
			return cache;
		}

		String key = GroupKey.getKey(dataId, group, tenant);
		cache = new CacheData(configFilterChainManager, agent.getName(), dataId, group, tenant);

		synchronized (cacheMap) {
			CacheData cacheFromMap = getCache(dataId, group, tenant);
			if (null != cacheFromMap) {
				cache = cacheFromMap;
				cache.setInitializing(true);
			}

			Map<String, CacheData> copy = new HashMap<>(cacheMap.get());
			copy.put(key, cache);
			cacheMap.set(copy);
		}

		log.info("[subscribe] {}", key);

		return cache;
	}

	public CacheData getCache(String dataId, String group) {
		return getCache(dataId, group, TenantUtil.getUserTenant());
	}

	public CacheData getCache(String dataId, String group, String tenant) {
		if (null == dataId || null == group) {
			throw new IllegalArgumentException();
		}

		return cacheMap.get().get(GroupKey.getKeyTenant(dataId, group, tenant));
	}

	public String getServerConfig(String dataId, String group, String tenant, long readTimeout) throws NacosException {
		if (StringUtils.isBlank(group)) {
			group = Constants.DEFAULT_GROUP;
		}

		HttpSimpleClient.HttpResult result = null;
		try {
			List<String> params = null;
			if (StringUtils.isBlank(tenant)) {
				params = Arrays.asList("dataId", dataId, "group", group);
			}
			else {
				params = Arrays.asList("dataId", dataId, "group", group, "tenant", tenant);
			}
			result = agent.httpGet(Constants.CONFIG_CONTROLLER_PATH, null, params, agent.getEncode(), readTimeout);
		}
		catch (IOException e) {
			log.error("[sub-server] get server config exception, dataId = {}, group = {}, tenant = {}, msg = {}",
					dataId, group, tenant, e.toString());
			throw new NacosException(NacosException.SERVER_ERROR, e.getMessage());
		}

		switch (result.code) {
			case HttpURLConnection.HTTP_OK:
				LocalConfigInfoProcessor.saveSnapshot(agent.getName(), dataId, group, tenant, result.content);
				return result.content;
			case HttpURLConnection.HTTP_NOT_FOUND:
				LocalConfigInfoProcessor.saveSnapshot(agent.getName(), dataId, group, tenant, null);
				return null;
			case HttpURLConnection.HTTP_CONFLICT:
				log.error("[sub-server-error] get server config being modified concurrently, dataId = {}, group = {}, tenant = {}",
						dataId, group, tenant);
				throw new NacosException(NacosException.CONFLICT, "data being modifie, dataId = " + dataId + ", group = " + group + ", tenant = " + tenant);
			case HttpURLConnection.HTTP_FORBIDDEN:
				log.error("[sub-server-error] no right, dataId = {}, group = {}, tenant = {}", dataId, group, tenant);
				throw new NacosException(result.code, result.content);
			default:
				log.error("[sub-server-error] dataId = {}, group = {}, tenant = {}, code = {}", dataId, group, tenant, result.code);
				throw new NacosException(result.code, "http error, code = " + result.code + ", dataId = " + dataId + ", group = " + group + ", tenant = " + tenant);
		}
	}

	private void checkLocalConfig(CacheData cacheData) {
		final String dataId = cacheData.getDataId();
		final String group = cacheData.getGroup();
		final String tenant = cacheData.getTenant();
		File path = LocalConfigInfoProcessor.getFailoverFile(agent.getName(), dataId, group, tenant);

		// 没有 -> 有
		if (!cacheData.isUseLocalConfigInfo() && path.exists()) {
			String content = LocalConfigInfoProcessor.getFailover(agent.getName(), dataId, group, tenant);
			String md5 = MD5.getInstance().getMD5String(content);
			cacheData.setUseLocalConfigInfo(true);
			cacheData.setLocalConfigLastModified(path.lastModified());
			cacheData.setContent(content);

			log.warn("[failover-change] failover file created. dataId = {}, group = {}, tenant = {}, md5 = {}, content = {}",
					dataId, group, tenant, md5, content);
			return;
		}

		// 有 -> 没有。不通知业务监听器，从server拿到配置后通知
		if (cacheData.isUseLocalConfigInfo() && !path.exists()) {
			cacheData.setUseLocalConfigInfo(false);
			log.warn("[failover-change] failover file deleted. dataId = {}, group = {}, tenant = {}", dataId, group, tenant);
			return;
		}

		// 有变更
		if (cacheData.isUseLocalConfigInfo() && path.exists() && cacheData.getLocalConfigLastModified() != path.lastModified()) {
			String content = LocalConfigInfoProcessor.getFailover(agent.getName(), dataId, group, tenant);
			String md5 = MD5.getInstance().getMD5String(content);
			cacheData.setUseLocalConfigInfo(true);
			cacheData.setLocalConfigLastModified(path.lastModified());
			cacheData.setContent(content);
			log.warn("[failover-change] failover file changed. dataId = {}, group = {}, tenant = {}, md5 = {}, content = {}", dataId, group, tenant, md5, content);
			return;
		}
	}

	private String null2DefaultGroup(String group) {
		return group == null ? Constants.DEFAULT_GROUP : group;
	}

	public void checkConfigInfo() {
		// 分任务
		int listenerSize = cacheMap.get().size();
		// 向上取整为批数
		int longingTaskCount = (int) Math.ceil(listenerSize / ParamUtil.getPerTaskConfigSize());
		if (longingTaskCount > currentLongingTaskCount) {
			for (int i = (int) currentLongingTaskCount; i < longingTaskCount; i++) {
				executorService.execute(new LongPullingRunnable(i));
			}
			currentLongingTaskCount = longingTaskCount;
		}
	}

	List<String> checkUpdateDataIds(List<CacheData> cacheDatas, List<String> inInitializingCacheList) {
		StringBuilder sb = new StringBuilder();
		for (CacheData cacheData : cacheDatas) {
			if (!cacheData.isUseLocalConfigInfo()) {
				sb.append(cacheData.getDataId()).append(Constants.WORD_SEPARATOR);
				sb.append(cacheData.getGroup()).append(Constants.WORD_SEPARATOR);
				if (StringUtils.isBlank(cacheData.getTenant())) {
					sb.append(cacheData.getMd5()).append(Constants.LINE_SEPARATOR);
				}
				else {
					sb.append(cacheData.getMd5()).append(Constants.WORD_SEPARATOR);
					sb.append(cacheData.getTenant()).append(Constants.LINE_SEPARATOR);
				}
				if (cacheData.isInitializing()) {
					// cacheData 首次出现在cacheMap中&首次check更新
					inInitializingCacheList.add(GroupKey.getKeyTenant(cacheData.getDataId(), cacheData.getGroup(), cacheData.getTenant()));
				}
			}
		}
		boolean isInitializingCacheList = !inInitializingCacheList.isEmpty();
		return checkUpdateConfigStr(sb.toString(), isInitializingCacheList);
	}

	List<String> checkUpdateConfigStr(String probeUpdateString, boolean isInitializingCacheList) {
		List<String> params = Arrays.asList(Constants.PROBE_MODIFY_REQUEST, probeUpdateString);
		long timeout = TimeUnit.SECONDS.toMillis(30L);

		List<String> headers = new ArrayList<>(2);
		headers.add("Long-Pulling-Timeout");
		headers.add("" + timeout);

		if (isInitializingCacheList) {
			headers.add("Long-Pulling-Timeout-No-Handup");
			headers.add("true");
		}

		if (StringUtils.isBlank(probeUpdateString)) {
			return Collections.emptyList();
		}

		try {
			HttpSimpleClient.HttpResult result = agent.httpPost(Constants.CONFIG_CONTROLLER_PATH + "/listener", headers, params, agent.getEncode(), timeout);

			if (HttpURLConnection.HTTP_OK == result.code) {
				setHealthServer(true);
				return parseUpdateDataIdResponse(result.content);
			}
			else {
				setHealthServer(false);
				if (HttpURLConnection.HTTP_INTERNAL_ERROR == result.code) {
					log.error("[check-update] get changed dataId error");
				}
				log.error("[check-update] get changed dataId error, code = {}", result.code);
			}
		}
		catch (IOException e) {
			setHealthServer(false);
			log.error("[check-update] get changed dataId exception, msg = {}", e.toString());
		}
		return Collections.emptyList();
	}

	private List<String> parseUpdateDataIdResponse(String response) {
		if (StringUtils.isBlank(response)) {
			return Collections.emptyList();
		}

		try {
			response = URLDecoder.decode(response, Constants.ENCODE);
		}
		catch (Exception e) {
			log.error("[polling-resp] decode modifiedDataIdsString error", e);
		}

		List<String> updateList = new LinkedList<>();

		for (String dataIdAndGroup : response.split(Constants.LINE_SEPARATOR)) {
			if (!StringUtils.isBlank(dataIdAndGroup)) {
				String[] keyArr = dataIdAndGroup.split(Constants.WORD_SEPARATOR);
				String dataId = keyArr[0];
				String group = keyArr[1];
				if (keyArr.length == 2) {
					updateList.add(GroupKey.getKey(dataId, group));
					log.info("[polling-resp] config changed. dataId = {}, group = {}", dataId, group);
				}
				else if (keyArr.length == 3) {
					String tenant = keyArr[2];
					updateList.add(GroupKey.getKeyTenant(dataId, group, tenant));
					log.info("[polling-resp] config changed. dataId = {}, group = {}, tenant = {}", dataId, group, tenant);
				}
				else {
					log.error("[polling-resp] invalid dataIdAndGroup = {}", dataIdAndGroup);
				}
			}
		}

		return updateList;
	}

	class LongPullingRunnable implements Runnable {

		private int taskId;

		public LongPullingRunnable(int taskId) {
			this.taskId = taskId;
		}

		@Override
		public void run() {
			try {
				List<CacheData> cacheDatas = new ArrayList<>();
				// check failover config
				for (CacheData cacheData : cacheMap.get().values()) {
					if (cacheData.getTaskId() == taskId) {
						cacheDatas.add(cacheData);
						try {
							checkLocalConfig(cacheData);
							if (cacheData.isUseLocalConfigInfo()) {
								cacheData.checkListenerMd5();
							}
						}
						catch (Exception e) {
							log.error("get local config info error", e);
						}
					}
				}

				List<String> inInitializingCacheList = new ArrayList<>();
				// check server config
				List<String> changedGroupKeys = checkUpdateDataIds(cacheDatas, inInitializingCacheList);

				for (String groupKey : changedGroupKeys) {
					String[] key = GroupKey.parseKey(groupKey);
					String dataId = key[0];
					String group = key[1];
					String tenant = null;
					if (key.length == 3) {
						tenant = key[2];
					}
					try {
						String content = getServerConfig(dataId, group, tenant, 3000L);
						CacheData cache = cacheMap.get().get(GroupKey.getKeyTenant(dataId, group, tenant));
						cache.setContent(content);
						log.info("[data-received] dataId = {}, group = {}, tenant = {}, md5 = {}, content = {}",
								dataId, group, tenant, cache.getMd5(), ContentUtils.truncateContent(content));
					}
					catch (NacosException e) {
						log.error("[get-update] get changed config exception, dataId = {}, group = {}, tenant = {}, msg = {}",
								dataId, group, tenant, e.getMessage());
					}
				}

				for (CacheData cacheData : cacheDatas) {
					if (!cacheData.isInitializing() || inInitializingCacheList.contains(GroupKey.getKeyTenant(cacheData.getDataId(), cacheData.getGroup(), cacheData.getTenant()))) {
						cacheData.checkListenerMd5();
						cacheData.setInitializing(false);
					}
				}

				inInitializingCacheList.clear();
			}
			catch (Throwable t) {
				log.error("longPulling error", t);
			}
			finally {
				executorService.execute(this);
			}
		}
	}
}

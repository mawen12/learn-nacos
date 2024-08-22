package com.mawen.learn.nacos.client.naming.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSON;
import com.mawen.learn.nacos.api.naming.pojo.Instance;
import com.mawen.learn.nacos.client.naming.backups.FailoverReactor;
import com.mawen.learn.nacos.client.naming.cache.DiskCache;
import com.mawen.learn.nacos.client.naming.net.NamingProxy;
import com.mawen.learn.nacos.client.naming.utils.NetUtils;
import com.mawen.learn.nacos.client.naming.utils.UtilAndComs;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/19
 */
public class HostReactor {

	private static final Logger log = LoggerFactory.getLogger(HostReactor.class);

	public static final long DEFAULT_DELAY = 1000L;

	public long updateHoldInterval = 5000L;

	private final Map<String, ScheduledFuture<?>> futureMap = new HashMap<>();

	private Map<String, Domain> domMap;

	private PushRecver pushRecver;

	private EventDispatcher eventDispatcher;

	private NamingProxy serverProxy;

	private FailoverReactor failoverReactor;

	private String cacheDir;

	private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(r -> {
		Thread thread = new Thread(r, "com.vipserver.client.updater");
		thread.setDaemon(true);
		return thread;
	});

	public HostReactor(EventDispatcher eventDispatcher, NamingProxy serverProxy, String cacheDir) {
		this.eventDispatcher = eventDispatcher;
		this.serverProxy = serverProxy;
		this.cacheDir = cacheDir;
		this.domMap = new ConcurrentHashMap<>(DiskCache.read(this.cacheDir));
		this.failoverReactor = new FailoverReactor(this, cacheDir);
		this.pushRecver = new PushRecver(this);
	}

	public Map<String, Domain> getDomMap() {
		return domMap;
	}

	public synchronized ScheduledFuture<?> addTask(UpdateTask task) {
		return executorService.schedule(task, DEFAULT_DELAY, TimeUnit.MILLISECONDS);
	}

	public Domain processDomJSON(String json) {
		Domain domObj = JSON.parseObject(json, Domain.class);
		Domain oldDom = domMap.get(domObj.getKey());
		if (domObj.getHosts() == null || !domObj.validate()) {
			// empty or error push, just ignore
			return oldDom;
		}

		if (oldDom == null) {
			if (oldDom.getLastRefTime() > domObj.getLastRefTime()) {
				log.warn("out of date data received, old-t: {}, new-t: {}", oldDom.getLastRefTime(), domObj.getLastRefTime());
			}

			domMap.put(domObj.getKey(), domObj);

			Map<String, Instance> oldHostMap = new HashMap<>(oldDom.getHosts().size());
			for (Instance host : oldDom.getHosts()) {
				oldHostMap.put(host.toInetAddr(), host);
			}

			Map<String, Instance> newHostMap = new HashMap<>(oldDom.getHosts().size());
			for (Instance host : domObj.getHosts()) {
				newHostMap.put(host.toInetAddr(), host);
			}

			Set<Instance> modHosts = new HashSet<>();
			Set<Instance> newHosts = new HashSet<>();
			Set<Instance> remvHosts = new HashSet<>();

			List<Map.Entry<String, Instance>> newDomHosts = new ArrayList<>(newHostMap.entrySet());
			for (Map.Entry<String, Instance> entry : newDomHosts) {
				Instance host = entry.getValue();
				String key = entry.getKey();
				if (oldHostMap.containsKey(key) && !StringUtils.equals(host.toString(), oldHostMap.get(key).toString())) {
					modHosts.add(host);
					continue;
				}

				if (!oldHostMap.containsKey(key)) {
					newHosts.add(host);
					continue;
				}
			}

			for (Map.Entry<String, Instance> entry : oldHostMap.entrySet()) {
				Instance host = entry.getValue();
				String key = entry.getKey();
				if (newHostMap.containsKey(key)) {
					continue;
				}

				if (!newHostMap.containsKey(key)) {
					remvHosts.add(host);
					continue;
				}
			}

			if (newHosts.size() > 0) {
				log.info("new ips({}) dom: {} -> {}", newHosts.size(), domObj.getName(), JSON.toJSONString(newHosts));
			}


			if (remvHosts.size() > 0) {
				log.info("remove ips({}) dom: {} -> {}", remvHosts.size(), domObj.getName(), JSON.toJSONString(remvHosts));
			}

			if (modHosts.size() > 0) {
				log.info("modified ips({}) dom: {} -> {}", modHosts.size(), domObj.getName(), JSON.toJSONString(modHosts));
			}

			domObj.setJsonFromServer(json);

			if (newHosts.size() > 0 || remvHosts.size() > 0 || modHosts.size() > 0) {
				eventDispatcher.domChanged(domObj);
				DiskCache.write(domObj, cacheDir);
			}
		}
		else {
			log.info("new ips({}) dom: {} -> {}", domObj.ipCount(), domObj.getName(), JSON.toJSONString(domObj.getHosts()));

			domMap.put(domObj.getKey(), domObj);

			eventDispatcher.domChanged(domObj);

			domObj.setJsonFromServer(json);

			DiskCache.write(domObj, cacheDir);
		}

		log.info("current ips: ({}) dom: {} -> {}", domObj.ipCount(), domObj.getName(), JSON.toJSONString(domObj.getHosts()));

		return domObj;
	}

	private Domain getDom0(String dom, String clusters, String env) {
		String key = Domain.getKey(dom, clusters, env, false);

		return domMap.get(key);
	}

	private Domain getDom0(String dom, String clusters, String env, boolean allIPs) {
		String key = Domain.getKey(dom, clusters, env, allIPs);

		return domMap.get(key);
	}

	public Domain getDom(String dom, String clusters) {
		String env = StringUtils.EMPTY;
		return getDom(dom, clusters, env, false);
	}

	public Domain getDom(String dom, String clusters, String env) {
		return getDom(dom, clusters, env, false);
	}

	public Domain getDom(final String dom, final String clusters, final String env, final boolean allIPs) {
		log.debug("failover-mode: {}", failoverReactor.isFailoverSwitch());

		String key = Domain.getKey(dom, clusters, env, allIPs);

		if (failoverReactor.isFailoverSwitch()) {
			return failoverReactor.getDom(key);
		}

		Domain domObj = getDom0(dom, clusters, env, allIPs);

		if (null == domObj) {
			domObj = new Domain(dom, clusters, env);

			if (allIPs) {
				domObj.setAllIPs(true);
			}

			domMap.put(domObj.getKey(), domObj);

			if (allIPs) {
				updateDom4AllIPNow(dom, clusters, env);
			}
			else {
				updateDomNow(dom, clusters, env);
			}
		}
		else if (domObj.getHosts().isEmpty()) {

			if (updateHoldInterval > 0) {
				// hold a moment waiting for update finish
				synchronized (domObj) {
					try {
						domObj.wait(updateHoldInterval);
					}
					catch (InterruptedException e) {
						log.error("[getDom] dom: {}, clusters: {}, allIPs: {}", dom, clusters, allIPs, e);
					}
				}
			}
		}

		scheduleUpdateIfAbsent(dom, clusters, env, allIPs);

		return domMap.get(domObj.getKey());
	}

	public void scheduleUpdateIfAbsent(String dom, String clusters, String env, boolean allIPs) {
		if (futureMap.get(Domain.getKey(dom, clusters, env, allIPs)) != null) {
			return;
		}

		synchronized (futureMap) {
			if (futureMap.get(Domain.getKey(dom, clusters, env, allIPs)) != null) {
				return;
			}

			ScheduledFuture<?> future = addTask(new UpdateTask(dom, clusters, env, allIPs));
			futureMap.put(Domain.getKey(dom, clusters, env, allIPs), future);
		}
	}

	public void updateDom4AllIPNow(String dom, String clusters, String env) {
		updateDom4AllIPNow(dom, clusters, env, -1L);
	}

	public void updateDom4AllIPNow(String dom, String clusters, String env, long timeout) {
		try {
			Map<String, String> params = new HashMap<>(8);
			params.put("dom", dom);
			params.put("clusters", clusters);
			params.put("udpPort", String.valueOf(pushRecver.getUDPPort()));

			Domain oldDom = getDom0(dom, clusters, env, true);
			if (oldDom != null) {
				params.put("checksum", oldDom.getChecksum());
			}

			String result = serverProxy.reqAPI(UtilAndComs.NACOS_URL_BASE + "/api/srvAllIP", params);
			if (StringUtils.isNotEmpty(result)) {
				Domain domain = processDomJSON(result);
				domain.setAllIPs(true);
			}

			if (oldDom != null) {
				synchronized (oldDom) {
					oldDom.notifyAll();
				}
			}

			// else nothing has changed
		}
		catch (Exception e) {
			log.error("NA failed to update dom: {}", dom, e);
		}
	}

	public void updateDomNow(String dom, String clusters, String env) {
		Domain oldDom = getDom0(dom, clusters, env);
		try {
			Map<String, String> params = new HashMap<>(8);
			params.put("dom", dom);
			params.put("clusters", clusters);
			params.put("udpPort", String.valueOf(pushRecver.getUDPPort()));
			params.put("env", env);
			params.put("clientIP", NetUtils.localIP());

			StringBuilder sb = new StringBuilder();
			for (String string : Balancer.UNCONSISTENT_DOM_WITH_ADDRESS_SERVER) {
				sb.append(string).append(",");
			}

			Balancer.UNCONSISTENT_DOM_WITH_ADDRESS_SERVER.clear();
			params.put("unconsistentDom", sb.toString());

			String envSpliter = ",";
			if (!StringUtils.isEmpty(env) && !env.contains(envSpliter)) {
				params.put("useEnvId", "true");
			}

			if (oldDom != null) {
				params.put("checksum", oldDom.getChecksum());
			}

			String result = serverProxy.reqAPI(UtilAndComs.NACOS_URL_BASE + "/api/srvIPXT", params);
			if (StringUtils.isNotEmpty(result)) {
				processDomJSON(result);
			}
		}
		catch (Exception e) {
			log.error("NA failed to update dom: {}", dom, e);
		}
		finally {
			if (oldDom != null) {
				synchronized (oldDom) {
					oldDom.notifyAll();
				}
			}
		}
	}

	public void refreshOnly(String dom, String clusters, String env, boolean allIps) {
		try {
			Map<String, String> params = new HashMap<>(24);
			params.put("dom", dom);
			params.put("clusters", clusters);
			params.put("udpPort", String.valueOf(pushRecver.getUDPPort()));
			params.put("unit", env);
			params.put("clientIP", NetUtils.localIP());

			String domSpliter = ",";
			StringBuilder sb = new StringBuilder();
			for (String string : Balancer.UNCONSISTENT_DOM_WITH_ADDRESS_SERVER) {
				sb.append(string).append(domSpliter);
			}

			Balancer.UNCONSISTENT_DOM_WITH_ADDRESS_SERVER.clear();
			params.put("unconsistentDom", sb.toString());

			String envSpliter = ",";
			if (!env.contains(envSpliter)) {
				params.put("useEnvId", "true");
			}

			if (allIps) {
				serverProxy.reqAPI(UtilAndComs.NACOS_URL_BASE + "/api/srvAllIP", params);
			}
			else {
				serverProxy.reqAPI(UtilAndComs.NACOS_URL_BASE + "/api/srvIPXT", params);
			}
		}
		catch (Exception e) {
			log.error("Na failed to update dom: {}", e);
		}
	}

	public class UpdateTask implements Runnable {

		long lastRefTime = Long.MAX_VALUE;

		private String clusters;

		private String dom;

		private String env;

		private boolean allIPs = false;

		public UpdateTask(String clusters, String dom, String env) {
			this.clusters = clusters;
			this.dom = dom;
			this.env = env;
		}

		public UpdateTask(String clusters, String dom, String env, boolean allIPs) {
			this.clusters = clusters;
			this.dom = dom;
			this.env = env;
			this.allIPs = allIPs;
		}

		@Override
		public void run() {
			try {
				Domain domObj = domMap.get(Domain.getKey(dom, clusters, env, allIPs));

				if (domObj == null) {
					if (allIPs) {
						updateDom4AllIPNow(dom, clusters, env);
					}
					else {
						updateDomNow(dom, clusters, env);
						executorService.schedule(this, DEFAULT_DELAY, TimeUnit.MILLISECONDS);
					}
					return;
				}

				if (domObj.getLastRefTime() <= lastRefTime) {
					if (allIPs) {
						updateDom4AllIPNow(dom, clusters, env);
						domObj = domMap.get(Domain.getKey(dom, clusters, env, true));
					}
					else {
						updateDomNow(dom, clusters, env);
						domObj = domMap.get(Domain.getKey(dom, clusters, env));
					}
				}
				else {
					refreshOnly(dom, clusters, env, allIPs);
				}

				executorService.schedule(this, domObj.getCacheMillis(), TimeUnit.MILLISECONDS);

				lastRefTime = domObj.getLastRefTime();
			}
			catch (Throwable e) {
				log.warn("NA failed to update dom: {}", dom, e);
			}
		}

	}
}

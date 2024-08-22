package com.mawen.learn.nacos.client.naming;

import java.beans.beancontext.BeanContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import com.mawen.learn.nacos.api.PropertyKeyConst;
import com.mawen.learn.nacos.api.exception.NacosException;
import com.mawen.learn.nacos.api.naming.NamingService;
import com.mawen.learn.nacos.api.naming.listener.EventListener;
import com.mawen.learn.nacos.api.naming.pojo.Cluster;
import com.mawen.learn.nacos.api.naming.pojo.Instance;
import com.mawen.learn.nacos.client.naming.beat.BeatInfo;
import com.mawen.learn.nacos.client.naming.beat.BeatReactor;
import com.mawen.learn.nacos.client.naming.core.Balancer;
import com.mawen.learn.nacos.client.naming.core.Domain;
import com.mawen.learn.nacos.client.naming.core.EventDispatcher;
import com.mawen.learn.nacos.client.naming.core.HostReactor;
import com.mawen.learn.nacos.client.naming.net.NamingProxy;
import com.mawen.learn.nacos.client.naming.utils.CollectionUtils;
import com.mawen.learn.nacos.client.naming.utils.UtilAndComs;
import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/19
 */
public class NacosNamingService implements NamingService {

	private String namespace;

	private String endpoint;

	private String serverList;

	private String cacheDir;

	private String logName;

	private HostReactor hostReactor;

	private BeatReactor beatReactor;

	private EventDispatcher eventDispatcher;

	private NamingProxy serverProxy;

	public NacosNamingService(String serverList) {
		this.serverList = serverList;
		init();
		this.eventDispatcher = new EventDispatcher();
		this.serverProxy = new NamingProxy(namespace, endpoint, serverList);
		this.beatReactor = new BeatReactor(serverProxy);
		this.hostReactor = new HostReactor(eventDispatcher, serverProxy, cacheDir);
	}

	public NacosNamingService(Properties properties) {
		init();

		this.serverList = properties.getProperty(PropertyKeyConst.SERVER_ADDR);

		String namespace = properties.getProperty(PropertyKeyConst.NAMESPACE);
		if (StringUtils.isNotEmpty(namespace)) {
			this.namespace = namespace;
		}

		String logName = properties.getProperty(UtilAndComs.NACOS_NAMING_LOG_NAME);
		if (StringUtils.isNotEmpty(logName)) {
			this.logName = logName;
		}

		String endpoint = properties.getProperty(PropertyKeyConst.ENDPOINT);
		if (StringUtils.isNotEmpty(endpoint)) {
			this.endpoint = endpoint + ":" + properties.getProperty("address.server.port", "8080");
		}

		this.cacheDir = System.getProperty("user.home") + "/nacos/naming/" + namespace;
		this.eventDispatcher = new EventDispatcher();
		this.serverProxy = new NamingProxy(namespace, endpoint, serverList);
		this.beatReactor = new BeatReactor(serverProxy);
		this.hostReactor = new HostReactor(eventDispatcher, serverProxy, cacheDir);
	}

	public void init() {

		namespace = System.getProperty(PropertyKeyConst.NAMESPACE);
		if (StringUtils.isEmpty(namespace)) {
			namespace = UtilAndComs.DEFAULT_NAMESPACE_ID;
		}

		logName = System.getProperty(UtilAndComs.NACOS_NAMING_LOG_NAME);
		if (StringUtils.isEmpty(logName)) {
			this.logName = "naming.log";
		}

		cacheDir = System.getProperty("com.mawen.learn.nacos.naming.cache.dir");
		if (StringUtils.isEmpty(cacheDir)) {
			cacheDir = System.getProperty("user.home") + "/nacos/naming/" + namespace;
		}
	}


	@Override
	public void registerInstance(String serviceName, String ip, int port) throws NacosException {
		registerInstance(serviceName, ip, port, StringUtils.EMPTY);
	}

	@Override
	public void registerInstance(String serviceName, String ip, int port, String clusterName) throws NacosException {
		Instance instance = new Instance();
		instance.setIp(ip);
		instance.setPort(port);
		instance.setWeight(1.0);
		instance.setCluster(new Cluster(clusterName));

		registerInstance(serviceName, instance);
	}

	@Override
	public void registerInstance(String serviceName, Instance instance) throws NacosException {
		BeatInfo beatInfo = new BeatInfo();
		beatInfo.setDom(serviceName);
		beatInfo.setIp(instance.getIp());
		beatInfo.setPort(instance.getPort());
		beatInfo.setCluster(instance.getCluster().getName());

		beatReactor.addBeatInfo(serviceName, beatInfo);

		serverProxy.registerService(serviceName, instance);
	}

	@Override
	public void deregisterInstance(String serviceName, String ip, int port) throws NacosException {
		deregisterInstance(serviceName, ip, port, StringUtils.EMPTY);
	}

	@Override
	public void deregisterInstance(String serviceName, String ip, int port, String clusterName) throws NacosException {
		beatReactor.removeBeatInfo(serviceName);
		serverProxy.deregisterService(serviceName, ip, port, clusterName);
	}

	@Override
	public List<Instance> getAllInstances(String serviceName) throws NacosException {
		return getAllInstances(serviceName, new ArrayList<>());
	}

	@Override
	public List<Instance> getAllInstances(String serviceName, List<String> clusters) throws NacosException {

		Domain domain = hostReactor.getDom(serviceName, StringUtils.join(clusters, ","), StringUtils.EMPTY, false);
		List<Instance> list;
		if (domain == null || CollectionUtils.isEmpty(list = domain.getHosts())) {
			throw new IllegalStateException("no host to srv for dom: " + serviceName);
		}
		return list;
	}

	@Override
	public List<Instance> selectInstances(String serviceName, boolean healthy) throws NacosException {
		return selectInstances(serviceName, new ArrayList<>(), healthy);
	}

	@Override
	public List<Instance> selectInstances(String serviceName, List<String> clusters, boolean healthy) throws NacosException {
		Domain domain = hostReactor.getDom(serviceName, StringUtils.join(clusters, ","), StringUtils.EMPTY, false);
		List<Instance> list;
		if (domain == null || CollectionUtils.isEmpty(list = domain.getHosts())) {
			throw new IllegalStateException("no host to srv for dom: " + serviceName);
		}

		if (healthy) {
			Iterator<Instance> iterator = list.iterator();
			while (iterator.hasNext()) {
				Instance instance = iterator.next();
				if  (!instance.isHealthy()) {
					iterator.remove();
				}
			}
		}
		else {
			Iterator<Instance> iterator = list.iterator();
			while (iterator.hasNext()) {
				Instance instance = iterator.next();
				if (instance.isHealthy()) {
					iterator.remove();
				}
			}
		}

		return list;
	}

	@Override
	public Instance selectOneHealthyInstance(String serviceName) throws NacosException {
		return selectOneHealthyInstance(serviceName, new ArrayList<>());
	}

	@Override
	public Instance selectOneHealthyInstance(String serviceName, List<String> clusters) throws NacosException {
		return Balancer.RandomByWeight.selectHost(hostReactor.getDom(serviceName, StringUtils.join(clusters, ",")));
	}

	@Override
	public void subscribe(String serviceName, EventListener listener) throws NacosException {
		eventDispatcher.addListener(hostReactor.getDom(serviceName, StringUtils.EMPTY), StringUtils.EMPTY, listener);
	}

	@Override
	public void subscribe(String serviceName, List<String> clusters, EventListener listener) throws NacosException {
		eventDispatcher.addListener(hostReactor.getDom(serviceName, StringUtils.join(clusters, ",")), StringUtils.join(clusters, ","), listener);
	}

	@Override
	public void unsubscribe(String serviceName, EventListener listener) throws NacosException {
		eventDispatcher.removeListener(serviceName, StringUtils.EMPTY, listener);
	}

	@Override
	public void unsubscribe(String serviceName, List<String> clusters, EventListener listener) throws NacosException {
		eventDispatcher.removeListener(serviceName, StringUtils.join(clusters, ","), listener);
	}
}

package com.mawen.learn.nacos.client.naming;

import java.util.Collections;
import java.util.List;

import com.mawen.learn.nacos.api.exception.NacosException;
import com.mawen.learn.nacos.api.naming.NamingService;
import com.mawen.learn.nacos.api.naming.listener.EventListener;
import com.mawen.learn.nacos.api.naming.pojo.Instance;

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

	private

	@Override
	public void registerInstance(String serviceName, String ip, String port) throws NacosException {

	}

	@Override
	public void registerInstance(String serviceName, String ip, String port, String clusterName) throws NacosException {

	}

	@Override
	public void registerInstance(String serviceName, Instance instance) throws NacosException {

	}

	@Override
	public void deregisterInstance(String serviceName, String ip, String port) throws NacosException {

	}

	@Override
	public void deregisterInstance(String serviceName, String ip, String port, String clusterName) throws NacosException {

	}

	@Override
	public List<Instance> getAllInstances(String serviceName) throws NacosException {
		return Collections.emptyList();
	}

	@Override
	public List<Instance> getAllInstances(String serviceName, List<String> clusters) throws NacosException {
		return Collections.emptyList();
	}

	@Override
	public List<Instance> selectInstances(String serviceName, boolean healthy) throws NacosException {
		return Collections.emptyList();
	}

	@Override
	public List<Instance> selectInstances(String serviceName, List<String> clusters, boolean healthy) throws NacosException {
		return Collections.emptyList();
	}

	@Override
	public Instance selectOneHealthyInstance(String serviceName) throws NacosException {
		return null;
	}

	@Override
	public Instance selectOneHealthyInstance(String serviceName, List<String> clusters) throws NacosException {
		return null;
	}

	@Override
	public void subscribe(String serviceName, EventListener listener) throws NacosException {

	}

	@Override
	public void subscribe(String serviceName, List<String> clusters, EventListener listener) throws NacosException {

	}

	@Override
	public void unsubscribe(String serviceName, EventListener listener) throws NacosException {

	}

	@Override
	public void unsubscribe(String serviceName, List<String> clusters, EventListener listener) throws NacosException {

	}
}

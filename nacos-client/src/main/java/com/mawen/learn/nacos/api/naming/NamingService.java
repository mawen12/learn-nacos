package com.mawen.learn.nacos.api.naming;

import java.util.List;

import com.mawen.learn.nacos.api.exception.NacosException;
import com.mawen.learn.nacos.api.naming.listener.EventListener;
import com.mawen.learn.nacos.api.naming.pojo.Instance;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/15
 */
public interface NamingService {

	void registerInstance(String serviceName, String ip, String port) throws NacosException;

	void registerInstance(String serviceName, String ip, String port, String clusterName) throws NacosException;

	void registerInstance(String serviceName, Instance instance) throws NacosException;

	void deregisterInstance(String serviceName, String ip, String port) throws NacosException;

	void deregisterInstance(String serviceName, String ip, String port, String clusterName) throws NacosException;

	List<Instance> getAllInstances(String serviceName) throws NacosException;

	List<Instance> getAllInstances(String serviceName, List<String> clusters) throws NacosException;

	List<Instance> selectInstances(String serviceName, boolean healthy) throws NacosException;

	List<Instance> selectInstances(String serviceName, List<String> clusters, boolean healthy) throws NacosException;

	Instance selectOneHealthyInstance(String serviceName) throws NacosException;

	Instance selectOneHealthyInstance(String serviceName, List<String> clusters) throws NacosException;

	void subscribe(String serviceName, EventListener listener) throws NacosException;

	void subscribe(String serviceName, List<String> clusters, EventListener listener) throws NacosException;

	void unsubscribe(String serviceName, EventListener listener) throws NacosException;

	void unsubscribe(String serviceName, List<String> clusters, EventListener listener) throws NacosException;

}

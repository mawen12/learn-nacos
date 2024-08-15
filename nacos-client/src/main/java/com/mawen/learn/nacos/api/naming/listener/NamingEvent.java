package com.mawen.learn.nacos.api.naming.listener;

import java.util.List;

import com.mawen.learn.nacos.api.naming.pojo.Instance;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/15
 */
public class NamingEvent implements Event{

	private String serviceName;

	private List<Instance> instances;

	public NamingEvent(String serviceName, List<Instance> instances) {
		this.serviceName = serviceName;
		this.instances = instances;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public List<Instance> getInstances() {
		return instances;
	}

	public void setInstances(List<Instance> instances) {
		this.instances = instances;
	}
}

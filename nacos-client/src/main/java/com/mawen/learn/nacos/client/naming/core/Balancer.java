package com.mawen.learn.nacos.client.naming.core;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.mawen.learn.nacos.api.naming.pojo.Instance;
import com.mawen.learn.nacos.client.naming.utils.CollectionUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/19
 */
public class Balancer {

	public static final List<String> UNCONSISTENT_DOM_WITH_ADDRESS_SERVER = new CopyOnWriteArrayList<>();

	public static class RandomByWeight {

		public static List<Instance> selectAll(Domain domain) {
			List<Instance> hosts = nothing(domain);

			if (CollectionUtils.isEmpty(hosts)) {
				throw new IllegalStateException("no host to srv for dom: " + domain.getName());
			}

			return hosts;
		}

		public static Instance selectHost(Domain domain) {

			List<Instance> hosts = selectAll(domain);

			if (CollectionUtils.isEmpty(hosts)) {
				throw new IllegalStateException("no host to srv for dom: " + domain.getName());
			}

			return getHostByRandomWeight(hosts);
		}

		public static List<Instance> nothing(Domain dom) {
			return dom.getHosts();
		}
	}

	protected static Instance getHostByRandomWeight(List<Instance> hosts) {
		// TODO start here
	}
}

package com.mawen.learn.nacos.client.naming.core;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.mawen.learn.nacos.api.naming.pojo.Instance;
import com.mawen.learn.nacos.client.naming.utils.Chooser;
import com.mawen.learn.nacos.client.naming.utils.CollectionUtils;
import com.mawen.learn.nacos.client.naming.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/19
 */
public class Balancer {

	private static final Logger log = LoggerFactory.getLogger(Balancer.class);

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
		log.debug("entry randomWithWeight");

		if (hosts == null || hosts.size() == 0) {
			log.debug("hosts == null || hosts.size() == 0");
			return null;
		}

		Chooser<String, Instance> vipChooser = new Chooser<>("www.mawen.com");

		log.debug("new Chooser");

		List<Pair<Instance>> hostsWithWeight = new ArrayList<>();
		for (Instance host : hosts) {
			if (host.isHealthy()) {
				hostsWithWeight.add(new Pair<>(host, host.getWeight()));
			}
		}
		log.debug("for (Host host : hosts)");
		vipChooser.refresh(hostsWithWeight);
		log.debug("vipChooser.refresh");
		return vipChooser.randomWithWeight();
	}
}

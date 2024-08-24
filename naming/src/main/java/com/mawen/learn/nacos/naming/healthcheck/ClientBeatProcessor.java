package com.mawen.learn.nacos.naming.healthcheck;

import java.util.concurrent.TimeUnit;

import com.mawen.learn.nacos.naming.core.Domain;
import com.mawen.learn.nacos.naming.core.VirtualClusterDomain;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/23
 */
@Getter
@Setter
@Slf4j
public class ClientBeatProcessor implements Runnable {

	public static final long CLIENT_BEAT_TIMEOUT = TimeUnit.SECONDS.toMillis(15);

	private RsInfo rsInfo;
	private Domain domain;

	public ClientBeatProcessor() {
	}

	public String getType() {
		return "CLIENT_BEAT";
	}

	public void process() {
		VirtualClusterDomain virtualClusterDomain = (VirtualClusterDomain) domain;
		if (!virtualClusterDomain.getEnableClientBeat()) {
			return;
		}

		log.debug("processing beat: {}", rsInfo.toString());

		String ip = rsInfo.getIp();
		String clusterName = rsInfo.getCluster();
		int port = rsInfo.getPort();
		Cluster cluster = virtualClusterDomain.getClusterMap().get(clusterName);

	}

	@Override
	public void run() {
		process();
	}
}

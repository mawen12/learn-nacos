package com.mawen.learn.nacos.naming.healthcheck;

import java.util.List;

import com.alibaba.fastjson.JSON;
import com.mawen.learn.nacos.naming.core.DistroMapper;
import com.mawen.learn.nacos.naming.core.IpAddress;
import com.mawen.learn.nacos.naming.core.VirtualClusterDomain;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/26
 */
@Slf4j
@AllArgsConstructor
public class ClientBeatCheckTask implements Runnable {

	private VirtualClusterDomain domain;

	@Override
	public void run() {
		try {
			if (!domain.getEnableClientBeat() || !DistroMapper.responsible(domain.getName())) {
				return;
			}

			List<IpAddress> ipAddresses = domain.allIPs();

			for (IpAddress ipAddress : ipAddresses) {
				if (System.currentTimeMillis() - ipAddress.getLastBeat() > ClientBeatProcessor.CLIENT_BEAT_TIMEOUT) {
					if (!ipAddress.isValid()) {
						ipAddress.setValid(false);
						log.info("{} {POS} {IP-DISABLED} valid: {}:{}@{}, region: {}, msg: client timeout after {}, last beat: {}",
								ipAddress.getClusterName(), ipAddress.getIp(), ipAddress.getPort(), ipAddress.getClusterName(),
								DistroMapper.LOCALHOST_SITE, ClientBeatProcessor.CLIENT_BEAT_TIMEOUT, ipAddress.getLastBeat());
						PushService.domChanged(domain.getName());
					}
				}

				if (System.currentTimeMillis() - ipAddress.getLastBeat() > domain.getIpDeleteTimeout()) {
					if (domain.allIPs().size() > 1) {
						log.info("dom: {}, ip: {}", domain.getName(), JSON.toJSONString(ipAddress));
						deleteIP(ipAddress);
					}
				}
			}
		}
		catch (Exception e) {
			log.warn("Exception while processing client beat time out.", e);
		}
		finally {
			HealthCheckReactor.scheduleCheck(this);
		}
	}
}

package com.mawen.learn.nacos.naming.healthcheck;

import com.mawen.learn.nacos.naming.core.Cluster;
import com.mawen.learn.nacos.naming.core.DistroMapper;
import com.mawen.learn.nacos.naming.core.VirtualClusterDomain;
import com.mawen.learn.nacos.naming.misc.Switch;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/26
 */
@Setter
@Getter
@Slf4j
public class HealthCheckTask implements Runnable {

	private Cluster cluster;

	private long checkRTNormalized = -1;

	private long checkRTBest = -1;

	private long checkRTWorst = -1;

	private long checkRTLast = -1;

	private long checkRTLastLast = -1;

	private long startTime;

	private volatile boolean cancelled = false;

	public HealthCheckTask(Cluster cluster) {
		this.cluster = cluster;
		initCheckRT();
	}

	public void initCheckRT() {
		checkRTNormalized = 2000 + RandomUtils.nextInt(0, Switch.getTcpHealthParams().getMax());

		checkRTBest = Long.MAX_VALUE;

		checkRTWorst = 0L;
	}

	@Override
	public void run() {
		AbstractHealthCheckProcessor processor = AbstractHealthCheckProcessor.getProcessor(cluster.getHealthChecker());

		try {
			if (DistroMapper.responsible(cluster.getDom().getName())) {
				processor.process(this);
				log.debug("schedule health check task: {}", cluster.getDom().getName());
			}
		}
		catch (Throwable e) {
			log.error("error while process health check for {}:{}", cluster.getDom().getName(), cluster.getName(), e);
		}
		finally {
			if (!cancelled) {
				HealthCheckReactor.scheduleCheck(this);

				if (this.getCheckRTLast() > 0
						&& Switch.isHealthCheckEnabled(cluster.getDom().getName)
						&& DistroMapper.responsible(cluster.getDom().getName)) {
					long diff = ((this.getCheckRTLast() - this.getCheckRTLastLast()) * 10000)
							/ this.getCheckRTLastLast();

					this.setCheckRTLastLast(this.getCheckRTLast());

					Cluster cluster = this.getCluster();
					if (cluster.getDom().getEnableHealthCheck()) {
						log.info("{}:{}@->normalized: {}, worst: {}, best: {}, last: {}, diff: {}",
								cluster.getDom().getName(), cluster.getName(),
								processor.getType(),
								this.getCheckRTNormalized(),
								this.getCheckRTWorst(),
								this.getCheckRTBest(),
								this.getCheckRTLast(),
								diff);
					}
				}
			}
		}
	}
}

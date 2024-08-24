package com.mawen.learn.nacos.naming.healthcheck;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

import com.mawen.learn.nacos.naming.core.IpAddress;
import com.mawen.learn.nacos.naming.misc.Switch;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/24
 */
public abstract class AbstractHealthCheckProcessor {

	private static final String HTTP_CHECK_MSG_PREFIX = "http:";

	public static final int CONNECT_TIMEOUT_MS = 500;

	private static LinkedBlockingDeque<HealthCheckResult> healthCheckResults = new LinkedBlockingDeque<>(1024 * 128);

	private void addResult(HealthCheckResult result) {

		if (!Switch)
	}

	@Getter
	@Setter
	@AllArgsConstructor
	static class HealthCheckResult {
		private String dom;
		private IpAddress ipAddress;
	}



}

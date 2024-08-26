package com.mawen.learn.nacos.naming.healthcheck;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/26
 */
public class HealthCheckReactor {

	private static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() / 2,
			r -> {
				Thread t = new Thread(r);
				t.setDaemon(true);
				t.setName("com.mawen.learn.nacos.naming.health");
				return t;
			});

	public static ScheduledFuture<?> scheduleCheck(HealthCheckTask task) {
		task.setStartTime(System.currentTimeMillis());

		return EXECUTOR.schedule(task, task.getCheckRTNormalized(), TimeUnit.MILLISECONDS);
	}

	public static ScheduledFuture<?> scheduleCheck(ClientBeatCheckTask task) {
		return EXECUTOR.schedule(task, 5000, TimeUnit.MILLISECONDS);
	}

	public static ScheduledFuture<?> scheduledNow(Runnable task) {
		return EXECUTOR.schedule(task, 0, TimeUnit.MILLISECONDS);
	}

}

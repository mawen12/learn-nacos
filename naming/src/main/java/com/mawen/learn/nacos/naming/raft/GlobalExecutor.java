package com.mawen.learn.nacos.naming.raft;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/24
 */
public class GlobalExecutor {

	public static final long HEARTBEAT_INTERVAL_MS = TimeUnit.SECONDS.toMillis(5L);

	public static final long LEADER_TIMEOUT_MS = TimeUnit.SECONDS.toMillis(15L);

	public static final long RANDOM_MS = TimeUnit.SECONDS.toMillis(5L);

	public static final long TICK_PERIOD_MS = TimeUnit.MILLISECONDS.toMillis(500L);

	public static final long ADDRESS_SERVER_UPDATE_INTERVAL_MS = TimeUnit.SECONDS.toMillis(5L);

	private static ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(2, r -> {
		Thread t = new Thread(r);
		t.setDaemon(true);
		t.setName("com.mawen.learn.nacos.naming.rafe.timer");
		return t;
	});

	public static void register(Runnable runnable) {
		executorService.scheduleAtFixedRate(runnable, 0, TICK_PERIOD_MS, TimeUnit.MILLISECONDS);
	}

	public static void register1(Runnable runnable) {
		executorService.scheduleWithFixedDelay(runnable, 0, TICK_PERIOD_MS, TimeUnit.MILLISECONDS);
	}

	public static void register(Runnable runnable, long delay) {
		executorService.scheduleAtFixedRate(runnable, 0, delay, TimeUnit.MILLISECONDS);
	}
}

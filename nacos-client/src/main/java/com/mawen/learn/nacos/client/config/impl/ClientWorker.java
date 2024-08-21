package com.mawen.learn.nacos.client.config.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;

import com.mawen.learn.nacos.client.config.filter.impl.ConfigFilterChainManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/21
 */
public class ClientWorker {

	private static final Logger log = LoggerFactory.getLogger(ClientWorker.class);

	private final ScheduledExecutorService executor;

	private final ExecutorService executorService;

	private AtomicReference<Map<String, CacheData>> cacheMap = new AtomicReference<>();

	private ServerHttpAgent agent;

	private ConfigFilterChainManager configFilterChainManager;

	private boolean isHealthServer = true;

	private double currentLongingTaskCount = 0;

	public boolean isHealthServer() {
		return isHealthServer;
	}

	private void setHealthServer(boolean isHealthServer) {
		this.isHealthServer = isHealthServer;
	}

	class LongPullingRunnable implements Runnable {

		private int taskId;

		public LongPullingRunnable(int taskId) {
			this.taskId = taskId;
		}

		@Override
		public void run() {
			List<CacheData> cacheDatas = new ArrayList<>();
			// check failover config
			for (CacheData cacheData : cacheMap.get().values()) {
				if (cacheData.)
			}
		}
	}
}

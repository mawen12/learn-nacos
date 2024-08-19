package com.mawen.learn.nacos.client.config.impl;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/18
 */
public class Limiter {

	public static final Logger log = LoggerFactory.getLogger(Limiter.class);

	private static final int CAPACITY_SIZE = 1000;
	private static final int LIMIT_TIME = 1000;
	private static Cache<String, RateLimiter> cache = CacheBuilder.newBuilder()
			.initialCapacity(CAPACITY_SIZE).expireAfterAccess(1, TimeUnit.MINUTES)
			.build();

	private static final String DEFAULT_LIMIT = "5";

	private static double limit = 5;

	static {
		try {
			String limitTimeStr = System.getProperty("limitTime", DEFAULT_LIMIT);
			limit = Double.parseDouble(limitTimeStr);
			log.info("limitTime:{}", limit);
		}
		catch (Exception e) {
			log.error("Nacos-xxx init limitTime fail", e);
		}
	}

	public static boolean isLimit(String accessKeyID) {
		RateLimiter rateLimiter = null;
		try {
			cache.get(accessKeyID, () -> RateLimiter.create(limit));
		}
		catch (ExecutionException e) {
			log.error("Nacos-xxx create limit fail", e);
		}

		if (rateLimiter != null && !rateLimiter.tryAcquire(LIMIT_TIME, TimeUnit.MILLISECONDS)) {
			log.error("Nacos-xxx access_key_id: {} limited", accessKeyID);
			return true;
		}
		return false;
	}
}

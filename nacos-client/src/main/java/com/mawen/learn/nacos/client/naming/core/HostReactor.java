package com.mawen.learn.nacos.client.naming.core;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/19
 */
public class HostReactor {

	public static final long DEFAULT_DELAY = 1000L;

	public long updateHoldInterval = 5000L;

	private final Map<String, ScheduledFuture<?>> futureMap = new HashMap<>();

	private Map<String, Domain> domMap;

	private PushRecver pushRecver;

	private EventDispatcher eventDispatcher;

	private NamingProxy serverProxy;

	private FailoverReactor failoverReactor;

	private String cacheDir;
}

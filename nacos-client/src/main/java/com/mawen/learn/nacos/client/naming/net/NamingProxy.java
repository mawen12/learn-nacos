package com.mawen.learn.nacos.client.naming.net;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/19
 */
public class NamingProxy {

	private String namespace;

	private String endpoint;

	private String nacosDomain;

	private List<String> serverList;

	private List<String> serversFromEndpoint = new ArrayList<>();

	private long lastSrvRefTime = 0L;

	private long vipSrvRefInterMillis = TimeUnit.SECONDS.toMillis(30);

	private ScheduledExecutorService executorService;

	// TODO start here
}

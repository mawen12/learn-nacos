package com.mawen.learn.nacos.client.naming.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.mawen.learn.nacos.api.naming.listener.EventListener;
import com.mawen.learn.nacos.api.naming.listener.NamingEvent;
import com.mawen.learn.nacos.api.naming.pojo.Instance;
import com.mawen.learn.nacos.client.naming.utils.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/19
 */
public class EventDispatcher {

	private static final Logger log = LoggerFactory.getLogger(EventDispatcher.class);
	private ExecutorService executor;

	private BlockingQueue<Domain> changedDoms = new LinkedBlockingQueue<>();

	private ConcurrentMap<String, List<EventListener>> observerMap = new ConcurrentHashMap<>();

	public EventDispatcher() {

		this.executor = Executors.newSingleThreadExecutor(r -> {
			Thread thread = new Thread(r, "com.mawen.learn.nacos.naming.client.listener");
			thread.setDaemon(true);

			return thread;
		});

		this.executor.execute(new Notifier());
	}

	public void addListener(Domain dom, String clusters, EventListener listener) {
		addListener(dom, clusters, StringUtils.EMPTY, listener);
	}

	public void addListener(Domain dom, String clusters, String env, EventListener listener) {
		List<EventListener> observers = Collections.synchronizedList(new ArrayList<>());
		observers.add(listener);

		observers = observerMap.putIfAbsent(Domain.getKey(dom.getName(), clusters, env), observers);
		if (observers != null) {
			observers.add(listener);
		}

		domChanged(dom);
	}

	public void removeListener(String dom, String clusters, EventListener listener) {
		String unit = "";

		List<EventListener> observers = observerMap.get(Domain.getKey(dom, clusters, unit));
		if (observers != null) {
			Iterator<EventListener> iter = observers.iterator();
			while (iter.hasNext()) {
				EventListener oldListener = iter.next();
				if (oldListener.equals(listener)) {
					iter.remove();
				}
			}
		}
	}

	public void domChanged(Domain dom) {
		if (dom == null) {
			return;
		}

		changedDoms.add(dom);
	}

	public void setExecutor(ExecutorService executor) {
		ExecutorService oldExecutor = this.executor;
		this.executor = executor;

		oldExecutor.shutdown();
	}

	private class Notifier implements Runnable {
		@Override
		public void run() {
			while (true) {
				Domain dom = null;
				try {
					dom = changedDoms.poll(5, TimeUnit.MINUTES);
				}
				catch (Exception ignored) {
				}

				if (dom == null) {
					continue;
				}

				try {
					List<EventListener> listeners = observerMap.get(dom.getKey());
					if (!CollectionUtils.isEmpty(listeners)) {
						for (EventListener listener : listeners) {
							List<Instance> hosts = Collections.unmodifiableList(dom.getHosts());
							if (!CollectionUtils.isEmpty(hosts)) {
								listener.onEvent(new NamingEvent(dom.getName(), hosts));
							}
						}
					}
				}
				catch (Exception e) {
					log.error("NA notify error from dom: {}, clusters: {}", dom.getName(), dom.getClusters(), e);
				}
			}
		}
	}
}

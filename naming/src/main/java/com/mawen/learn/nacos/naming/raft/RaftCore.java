package com.mawen.learn.nacos.naming.raft;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.mawen.learn.nacos.naming.core.VirtualClusterDomain;
import com.mawen.learn.nacos.naming.misc.UtilsAndCommons;
import lombok.extern.slf4j.Slf4j;
import org.javatuples.Pair;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/24
 */
@Slf4j
public class RaftCore {

	public static final String API_VOTE = UtilsAndCommons.NACOS_NAMING_CONTEXT + "/raft/vote";

	public static final String API_BEAT = UtilsAndCommons.NACOS_NAMING_CONTEXT + "/raft/beat";

	public static final String API_PUB = UtilsAndCommons.NACOS_NAMING_CONTEXT + "/raft/publish";

	public static final String API_UNSF_PUB = UtilsAndCommons.NACOS_NAMING_CONTEXT + "/raft/unSafePublish";

	public static final String API_DEL = UtilsAndCommons.NACOS_NAMING_CONTEXT + "/raft/delete";

	public static final String API_GET = UtilsAndCommons.NACOS_NAMING_CONTEXT + "/raft/get";

	public static final String API_ON_PUB = UtilsAndCommons.NACOS_NAMING_CONTEXT + "/raft/onPublish";

	public static final String API_ON_DEL = UtilsAndCommons.NACOS_NAMING_CONTEXT + "/raft/onDelete";

	public static final String API_GET_PEER = UtilsAndCommons.NACOS_NAMING_CONTEXT + "/raft/getPeer";

	private static ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1, r -> {
		Thread t = new Thread(r);
		t.setDaemon(true);
		t.setName("com.mawen.learn.nacos.naming.rafe.notifier");
		return t;
	});

	public static final Lock OPERATE_LOCK = new ReentrantLock();

	public static final int PUBLISH_TERM_INCREASE_COUNT = 100;

	private static final int INIT_LOCK_TIME_SECONDS = 3;

	private static volatile  boolean initialized = false;

	private static Lock lock = new ReentrantLock();

	private static volatile List<RaftListener> listeners = new CopyOnWriteArrayList<>();

	private static ConcurrentHashMap<String, Datum> datums = new ConcurrentHashMap<>();

	private static PeerSet peers = new PeerSet();

	private static volatile Notifier notifier = new Notifier();

	public static void init() {
		log.info("initializing Raft sub-system");

		executor.submit(notifier);

		peers.add(NamingProxy.getServers());
	}

	@Slf4j
	public static class Notifier implements Runnable {

		private BlockingQueue<Pair> tasks = new LinkedBlockingQueue<>(1024 * 1024);

		private void addTask(Datum datum, ApplyAction action) {
			tasks.add(Pair.with(datum, action));
		}

		@Override
		public void run() {
			log.info("raft notifier started");

			while (true) {
				try {
					Pair pair = tasks.take();

					if (pair == null) {
						continue;
					}

					Datum datum = (Datum) pair.getValue0();
					ApplyAction action = (ApplyAction) pair.getValue1();
					int count = 0;
					for (RaftListener listener : listeners) {

						if (listener instanceof VirtualClusterDomain) {
							log.debug("listener: {}", ((VirtualClusterDomain) listener).getName());
						}

						if (!listener.interests(datum.key)) {
							continue;
						}

						count++;

						try {
							if (action == ApplyAction.CHANGE) {
								listener.onChange(datum.key, datum.value);
								continue;
							}

							if (action == ApplyAction.DELETE) {
								listener.onDelete(datum.key, datum.value);
								continue;
							}
						}
						catch (Throwable e) {
							log.error("error while notifying listener of key: {}", datum.key, e);
						}
					}

					log.debug("datum change notified, key: {}, listener count: {}", datum.key, count);
				}
				catch (Throwable e) {
					log.error("Error while handling notifying task", e);
				}
			}
		}

		public enum ApplyAction {
			CHANGE,

			DELETE
		}
	}
}

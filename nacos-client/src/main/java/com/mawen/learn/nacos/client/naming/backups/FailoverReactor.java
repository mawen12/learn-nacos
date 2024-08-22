package com.mawen.learn.nacos.client.naming.backups;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSON;
import com.mawen.learn.nacos.client.naming.cache.DiskCache;
import com.mawen.learn.nacos.client.naming.core.Domain;
import com.mawen.learn.nacos.client.naming.core.HostReactor;
import com.mawen.learn.nacos.client.naming.utils.CollectionUtils;
import com.mawen.learn.nacos.client.naming.utils.UtilAndComs;
import com.mawen.learn.nacos.client.utils.ConcurrentDiskUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/19
 */
public class FailoverReactor {

	private static final Logger log = LoggerFactory.getLogger(FailoverReactor.class);

	private static final long DAY_PERIOD_MINUTES = 24 * 60;

	private String failoverDir;

	private HostReactor hostReactor;

	private Map<String, Domain> domainMap = new ConcurrentHashMap<>();

	private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(r -> {
		Thread thread = new Thread(r);
		thread.setDaemon(true);
		thread.setName("com.mawen.learn.nacos.naming.failover");
		return thread;
	});

	private Map<String, String> switchParams = new ConcurrentHashMap<>();


	public FailoverReactor(HostReactor hostReactor, String cacheDir) {
		this.hostReactor = hostReactor;
		this.failoverDir = cacheDir + "/failover";
		this.init();
	}

	public void init() {
		this.executorService.scheduleWithFixedDelay(new SwitchRefresher(), 0L, 5000L, TimeUnit.MILLISECONDS);

		this.executorService.scheduleWithFixedDelay(new DiskFileWriter(), 30, DAY_PERIOD_MINUTES, TimeUnit.MINUTES);

		this.executorService.schedule(() -> {
			try {
				File cacheDir = new File(failoverDir);

				if (!cacheDir.exists() && !cacheDir.mkdir()) {
					throw new IllegalStateException("failed to create cache dir: " + failoverDir);
				}

				File[] files = cacheDir.listFiles();
				if (files == null || files.length <= 0) {
					new DiskFileWriter().run();
				}
			}
			catch (Throwable e) {
				log.error("NA failed to backup file on startup.", e);
			}
		}, 10000L, TimeUnit.MILLISECONDS);
	}

	public Date addDay(Date date, int num) {
		Calendar startDT = Calendar.getInstance();
		startDT.setTime(date);
		startDT.add(Calendar.DAY_OF_MONTH, num);
		return startDT.getTime();
	}

	public boolean isFailoverSwitch() {
		return Boolean.parseBoolean(switchParams.get("failover-mode"));
	}

	public Domain getDom(String key) {
		Domain domain = domainMap.get(key);

		if (domain == null) {
			domain = new Domain();
			domain.setName(key);
		}

		return domain;
	}

	class SwitchRefresher implements Runnable {

		long lastModifiedMillis = 0L;

		@Override
		public void run() {
			try {
				File switchFile = new File(failoverDir + UtilAndComs.FAILOVER_SWITCH);
				if (!switchFile.exists()) {
					switchParams.put("failover-mode", "false");
					log.debug("failover switch is not found, {}", switchFile.getName());
					return;
				}

				long modified = switchFile.lastModified();

				if (lastModifiedMillis < modified) {
					lastModifiedMillis = modified;
					String failover = ConcurrentDiskUtil.getFileContent(failoverDir + UtilAndComs.FAILOVER_SWITCH, Charset.defaultCharset().toString());
					if (!StringUtils.isEmpty(failover)) {
						List<String> lines = Arrays.asList(failover.split(DiskCache.getLineSeparator()));

						for (String line : lines) {
							String line1 = line.trim();
							if ("1".equals(line1)) {
								switchParams.put("failover-mode", "true");
								log.info("failover-mode is on");
								new FailoverFileReader().run();
							}
							else if ("0".equals(line1)) {
								switchParams.put("failover-mode", "false");
								log.info("failover-mode is off");
							}
						}
					}
					else {
						switchParams.put("failover-mode", "false");
					}
				}
			}
			catch (Exception e) {
				log.error("NA failed to read failover switch.", e);
			}
		}
	}

	class FailoverFileReader implements Runnable {

		@Override
		public void run() {
			Map<String, Domain> domMap = new HashMap<>(16);

			BufferedReader reader = null;
			try {
				File cacheDir = new File(failoverDir);
				if (!cacheDir.exists() && !cacheDir.mkdir()) {
					throw new IllegalStateException("failed to create cache dir: " + failoverDir);
				}

				File[] files = cacheDir.listFiles();
				if (files == null) {
					return;
				}

				for (File file : files) {
					if (!file.isFile()) {
						continue;
					}

					if (file.getName().equals(UtilAndComs.FAILOVER_SWITCH)) {
						continue;
					}

					Domain dom = new Domain(file.getName());

					try {
						String dataString = ConcurrentDiskUtil.getFileContent(file, Charset.defaultCharset().toString());
						reader = new BufferedReader(new StringReader(dataString));

						String json;
						if ((json = reader.readLine()) != null) {
							try {
								dom = JSON.parseObject(json, Domain.class);
							}
							catch (Exception e) {
								log.error("NA error while parsing cached dom: {}", json, e);
							}
						}
					}
					catch (Exception e) {
						log.error("NA failed to read cache from dom: {}", file.getName(), e);
					}
					finally {
						try {
							if (reader != null) {
								reader.close();
							}
						}
						catch (IOException e) {
							// ignored
						}
					}

					if (!CollectionUtils.isEmpty(dom.getHosts())) {
						domMap.put(dom.getKey(), dom);
					}
				}
			}
			catch (Exception e) {
				log.error("NA failed to read cache file", e);
			}

			if (domMap.size() > 0) {
				domainMap = domMap;
			}
		}
	}

	class DiskFileWriter extends TimerTask {

		@Override
		public void run() {
			Map<String, Domain> map = hostReactor.getDomMap();
			for (Map.Entry<String, Domain> entry : map.entrySet()) {
				Domain domain = entry.getValue();
				if (StringUtils.equals(domain.getKey(), UtilAndComs.ALL_IPS) || StringUtils.equals(domain.getName(), UtilAndComs.ENV_LIST_KEY)
						|| StringUtils.equals(domain.getName(), "00-00---000-ENV_CONFIGS-000---00-00")
						|| StringUtils.equals(domain.getName(), "vipclient.properties")
						|| StringUtils.equals(domain.getName(), "00-00---000-ALL_HOSTS-000---00-00")) {
					continue;
				}

				DiskCache.write(domain, failoverDir);
			}
		}
	}
}

package com.mawen.learn.nacos.client.naming.beat;

import java.sql.Time;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mawen.learn.nacos.client.naming.net.NamingProxy;
import com.mawen.learn.nacos.client.naming.utils.UtilAndComs;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/22
 */
public class BeatReactor {

	private ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1,r -> {
		Thread t = new Thread(r);
		t.setName("com.mawen.learn.nacos.naming.beat.sender");
		t.setDaemon(true);
		return t;
	});

	private long clientBeatInterval = 10 * 1000;

	private NamingProxy serverProxy;

	private final Map<String, BeatInfo> dom2Beat = new ConcurrentHashMap<>();

	public BeatReactor(NamingProxy serverProxy) {
		this.serverProxy = serverProxy;
		this.executorService.scheduleAtFixedRate(new BeatProcessor(), 0, clientBeatInterval, TimeUnit.MILLISECONDS);
	}

	public void addBeatInfo(String dom, BeatInfo beatInfo) {
		dom2Beat.put(dom, beatInfo);
	}

	public void removeBeatInfo(String dom) {
		dom2Beat.remove(dom);
	}

	class BeatProcessor implements Runnable {

		private final Logger log = LoggerFactory.getLogger(BeatProcessor.class);

		@Override
		public void run() {
			try {
				for (Map.Entry<String, BeatInfo> entry : dom2Beat.entrySet()) {
					BeatInfo beatInfo = entry.getValue();
					executorService.schedule(new BeatTask(beatInfo), 0, TimeUnit.MILLISECONDS);
					log.info("send beat to server: {}", beatInfo.toString());
				}
			}
			catch (Exception e) {
				log.error("Exception while scheduling beant.", e);
			}
		}
	}

	class BeatTask implements Runnable {

		private final Logger log = LoggerFactory.getLogger(BeatTask.class);

		private BeatInfo beatInfo;

		public BeatTask(BeatInfo beatInfo) {
			this.beatInfo = beatInfo;
		}

		@Override
		public void run() {
			Map<String ,String> params = new HashMap<>(2);
			params.put("beat", JSON.toJSONString(beatInfo));
			params.put("dom", beatInfo.getDom());

			try {
				String result = serverProxy.callAllServers(UtilAndComs.NACOS_URL_BASE + "/api/clientBeat", params);
				JSONObject jsonObject = JSON.parseObject(result);

				if (jsonObject != null) {
					clientBeatInterval = jsonObject.getLong("clientBeatInterval");
				}
			}
			catch (Exception e) {
				log.error("failed to send beat: {}", JSON.toJSONString(beatInfo), e);
			}
		}
	}
}

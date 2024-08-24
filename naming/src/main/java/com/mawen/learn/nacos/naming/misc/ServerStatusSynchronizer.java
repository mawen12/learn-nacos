package com.mawen.learn.nacos.naming.misc;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import com.mawen.learn.nacos.naming.boot.RunningConfig;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.Response;
import lombok.extern.slf4j.Slf4j;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/24
 */
@Slf4j
public class ServerStatusSynchronizer implements Synchronizer {

	@Override
	public void send(String serverIP, Message msg) {
		if (serverIP == null) {
			return;
		}

		final Map<String, String> params = new HashMap<>(2);

		params.put("serverStatus", msg.getData());

		String url = "http://" + serverIP + ":" + RunningConfig.getServerPort()
				+ RunningConfig.getContextPath() + UtilsAndCommons.NACOS_NAMING_CONTEXT
				+ "/api/serverStatus";

		if (serverIP.contains(UtilsAndCommons.CLUSTER_CONF_IP_SPLITTER)) {
			url = "http://" + serverIP + RunningConfig.getContextPath()
					+ UtilsAndCommons.NACOS_NAMING_CONTEXT + "/api/serverStatus";
		}

		try {
			HttpClient.asyncHttpGet(url, null, params, new AsyncCompletionHandler() {
				@Override
				public Integer onCompleted(Response response) throws Exception {
					if (response.getStatusCode() != HttpURLConnection.HTTP_OK) {
						log.warn("failed to request serverStatus, remote server: {}", serverIP);
						return 1;
					}
					return 0;
				}
			});
		}
		catch (Exception e) {
			log.warn("failed to request serverStatus, remote server: {}", serverIP, e);
		}
	}

	@Override
	public Message get(String serverIP, String key) {
		return null;
	}
}

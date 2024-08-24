package com.mawen.learn.nacos.naming.misc;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import com.mawen.learn.nacos.naming.boot.RunningConfig;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/24
 */
@Slf4j
public class DomainStatusSynchronizer implements Synchronizer {

	@Override
	public void send(String serverIP, Message msg) {
		if (serverIP == null) {
			return;
		}

		Map<String, String> params = new HashMap<>(10);
		params.put("domsStatus", msg.getData());
		params.put("clientIP", serverIP);

		String url = "http://" + serverIP + ":" + RunningConfig.getServerPort() + RunningConfig.getContextPath()
				+ UtilsAndCommons.NACOS_NAMING_CONTEXT + "/api/domStatus";

		if (serverIP.contains(UtilsAndCommons.CLUSTER_CONF_IP_SPLITTER)) {
			url = "http://" + serverIP + RunningConfig.getContextPath() + UtilsAndCommons.NACOS_NAMING_CONTEXT + "/api/domStatus";
		}

		try {
			HttpClient.asyncHttpPost(url, null, params, new AsyncCompletionHandler() {
				@Override
				public Integer onCompleted(Response response) throws Exception {
					if (response.getStatusCode() != HttpURLConnection.HTTP_OK) {
						log.warn("failed to request domStatus, remote server: {}", serverIP);
						return 1;
					}
					return 0;
				}
			});
		}
		catch (Exception e) {
			log.warn("failed to request domStatus, remote server: {}", serverIP, e);
		}
	}

	@Override
	public Message get(String serverIP, String key) {

		if (serverIP == null) {
			return null;
		}

		Map<String ,String> params = new HashMap<>(10);
		params.put("dom", key);

		String result;
		try {
			log.info("sync dom status from: {}, dom: {}", serverIP, key);
			result = NamingProxy.reqAPI("ip4Dom2", params, serverIP, false);
		}
		catch (Exception e) {
			log.warn("Failed to get domain status from {}", serverIP, e);
			return null;
		}

		if (result == null || result.equals(StringUtils.EMPTY)) {
			return null;
		}

		Message msg = new Message();
		msg.setData(result);

		return msg;
	}
}

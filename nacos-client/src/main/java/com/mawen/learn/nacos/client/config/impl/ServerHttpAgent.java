package com.mawen.learn.nacos.client.config.impl;


import java.util.Date;
import java.util.List;

import com.mawen.learn.nacos.client.config.impl.HttpSimpleClient.HttpResult;
import org.codehaus.jackson.annotate.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/18
 */
public class ServerHttpAgent {

	public static final Logger logger = LoggerFactory.getLogger(ServerHttpAgent.class);

	private String accessKey;

	private String secretKey;

	private String encode;

	private volatile STSCredential sTSCredential;

	private final ServerListManager serverListManager;

	public HttpResult httpGet(String path, List<String> headers, List<String> paramValues, String encoding, long readTimeoutMs) {
		final long endTime = System.currentTimeMillis() + readTimeoutMs;

		boolean isSSL = false;

		do {
			try {
				List<String> newHeaders = getSpasHeaders(paramValues);
				if (headers != null) {
					newHeaders.addAll(headers);
				}
				HttpResult result = HttpSimpleClient.httpGet(getUrl());
			}
		} while (System.currentTimeMillis() > endTime);
	}

	public String getName() {
		return serverListManager.getName();
	}

	private static class STSCredential {
		@JsonProperty("AccessKeyId")
		private String accessKeyId;

		@JsonProperty("AccessKeySecret")
		private String accessKeySecret;

		@JsonProperty("Expiration")
		private Date expiration;

		@JsonProperty("SecurityToken")
		private String securityToken;

		@JsonProperty("LastUpdated")
		private Date lastUpdated;

		@JsonProperty("Code")
		private String code;

		public String getAccessKeyId() {
			return accessKeyId;
		}

		public Date getExpiration() {
			return expiration;
		}

		public Date getLastUpdated() {
			return lastUpdated;
		}

		public String getCode() {
			return code;
		}

		@Override
		public String toString() {
			return "STSCredential{" +
					"accessKeyId='" + accessKeyId + '\'' +
					", accessKeySecret='" + accessKeySecret + '\'' +
					", expiration=" + expiration +
					", securityToken='" + securityToken + '\'' +
					", lastUpdated=" + lastUpdated +
					", code='" + code + '\'' +
					'}';
		}
	}
}

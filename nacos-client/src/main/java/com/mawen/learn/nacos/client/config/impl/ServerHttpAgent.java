package com.mawen.learn.nacos.client.config.impl;


import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import com.mawen.learn.nacos.api.PropertyKeyConst;
import com.mawen.learn.nacos.api.exception.NacosException;
import com.mawen.learn.nacos.client.config.common.Constants;
import com.mawen.learn.nacos.client.config.impl.HttpSimpleClient.HttpResult;
import com.mawen.learn.nacos.client.identify.STSConfig;
import com.mawen.learn.nacos.client.utils.JSONUtils;
import com.mawen.learn.nacos.client.utils.ParamUtil;
import com.mawen.learn.nacos.common.util.IoUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.type.TypeReference;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/18
 */
@Slf4j
public class ServerHttpAgent {

	private String accessKey;

	private String secretKey;

	private String encode;

	private volatile STSCredential sTSCredential;

	private final ServerListManager serverListManager;

	public ServerHttpAgent(ServerListManager serverListManager) {
		this.serverListManager = serverListManager;
	}

	public ServerHttpAgent(ServerListManager serverListManager, Properties properties) {
		this.serverListManager = serverListManager;
		String accessKey = properties.getProperty(PropertyKeyConst.ACCESS_KEY);
		if (StringUtils.isBlank(accessKey)) {
			this.accessKey = SpasAdapter.getAccessKey();
		}
		else {
			this.accessKey = accessKey;
		}

		String secretKey = properties.getProperty(PropertyKeyConst.SECRET_KEY);
		if (StringUtils.isBlank(secretKey)) {
			this.secretKey = SpasAdapter.getSecretKey();
		}
		else {
			this.secretKey = secretKey;
		}
	}

	public ServerHttpAgent(Properties properties) throws NacosException {
		String encode = properties.getProperty(PropertyKeyConst.ENCODE);
		if (StringUtils.isBlank(encode)) {
			this.encode = Constants.ENCODE;
		}
		else {
			this.encode = encode.trim();
		}

		this.serverListManager = new ServerListManager(properties);

		String accessKey = properties.getProperty(PropertyKeyConst.ACCESS_KEY);
		if (StringUtils.isBlank(accessKey)) {
			this.accessKey = SpasAdapter.getAccessKey();
		}
		else {
			this.accessKey = accessKey;
		}

		String secretKey = properties.getProperty(PropertyKeyConst.SECRET_KEY);
		if (StringUtils.isBlank(secretKey)) {
			this.secretKey = SpasAdapter.getSecretKey();
		}
		else {
			this.secretKey = secretKey;
		}
	}

	public synchronized void start() throws NacosException {
		serverListManager.start();
	}

	public HttpResult httpGet(String path, List<String> headers, List<String> paramValues, String encoding, long readTimeoutMs) throws IOException {
		final long endTime = System.currentTimeMillis() + readTimeoutMs;

		boolean isSSL = false;

		do {
			try {
				List<String> newHeaders = getSpasHeaders(paramValues);
				if (headers != null) {
					newHeaders.addAll(headers);
				}
				HttpResult result = HttpSimpleClient.httpGet(getUrl(serverListManager.getCurrentServerAddr(), path, isSSL), newHeaders, paramValues, encoding, readTimeoutMs, isSSL);
				if (result.code == HttpURLConnection.HTTP_INTERNAL_ERROR
						|| result.code == HttpURLConnection.HTTP_BAD_GATEWAY
						|| result.code == HttpURLConnection.HTTP_UNAVAILABLE) {
					log.error("currentServerAddr: {}, httpCode: {}", serverListManager.getCurrentServerAddr(), result.code);
				}
				else {
					return result;
				}
			}
			catch (ConnectException | SocketTimeoutException e) {
				log.error("currentServerAddr: {}", serverListManager.getCurrentServerAddr(), e);
				serverListManager.refreshCurrentServerAddr();
			}
			catch (IOException e) {
				log.error("currentServerAddr: {}", serverListManager.getCurrentServerAddr(), e);
				throw e;
			}
		} while (System.currentTimeMillis() > endTime);

		log.error("no available server");
		throw new ConnectException("no available server");
	}

	public HttpResult httpPost(String path, List<String> headers, List<String> paramValues, String encoding, long readTimeoutMs) throws IOException {
		final long endTime = System.currentTimeMillis() + readTimeoutMs;
		boolean isSSL = false;
		do {
			try {
				List<String> newHeaders = getSpasHeaders(paramValues);
				if (headers != null) {
					headers.addAll(headers);
				}
				HttpResult result = HttpSimpleClient.httpPost(getUrl(serverListManager.getCurrentServerAddr(), path, isSSL), newHeaders, paramValues, encoding, readTimeoutMs, isSSL);

				if (result.code == HttpURLConnection.HTTP_INTERNAL_ERROR
						|| result.code == HttpURLConnection.HTTP_BAD_GATEWAY
						|| result.code == HttpURLConnection.HTTP_UNAVAILABLE) {
					log.error("currentServerAddr: {}, httpCode: {}", serverListManager.getCurrentServerAddr(), result.code);
				}
				else {
					return result;
				}
			}
			catch (ConnectException | SocketTimeoutException e) {
				log.error("currentServerAddr: {}", serverListManager.getCurrentServerAddr(), e);
				serverListManager.refreshCurrentServerAddr();
			}
			catch (IOException e) {
				log.error("currentServerAddr: {}", serverListManager.getCurrentServerAddr(), e);
				throw e;
			}
		} while (System.currentTimeMillis() < endTime);

		log.error("no available server");
		throw new ConnectException("no available server");
	}

	public HttpResult httpDelete(String path, List<String> headers, List<String> paramValues, String encoding, long readTimeoutMs) throws IOException {
		final long endTime = System.currentTimeMillis() + readTimeoutMs;
		boolean isSSL = false;
		do {
			try {
				List<String> newHeaders = getSpasHeaders(paramValues);
				if (headers != null) {
					headers.addAll(headers);
				}
				HttpResult result = HttpSimpleClient.httpDelete(getUrl(serverListManager.getCurrentServerAddr(), path, isSSL), newHeaders, paramValues, encoding, readTimeoutMs, isSSL);

				if (result.code == HttpURLConnection.HTTP_INTERNAL_ERROR
						|| result.code == HttpURLConnection.HTTP_BAD_GATEWAY
						|| result.code == HttpURLConnection.HTTP_UNAVAILABLE) {
					log.error("currentServerAddr: {}, httpCode: {}", serverListManager.getCurrentServerAddr(), result.code);
				}
				else {
					return result;
				}
			}
			catch (ConnectException | SocketTimeoutException e) {
				log.error("currentServerAddr: {}", serverListManager.getCurrentServerAddr(), e);
				serverListManager.refreshCurrentServerAddr();
			}
			catch (IOException e) {
				log.error("currentServerAddr: {}", serverListManager.getCurrentServerAddr(), e);
				throw e;
			}
		} while (System.currentTimeMillis() < endTime);

		log.error("no available server");
		throw new ConnectException("no available server");
	}

	public String getName() {
		return serverListManager.getName();
	}

	public String getTenant() {
		return serverListManager.getTenant();
	}

	public String getEncode() {
		return encode;
	}

	public static String getAppName() {
		return ParamUtil.getAppName();
	}

	private String getUrl(String serverAddr, String relativePath, boolean isSSL) {
		String httpPrefix = "http://";
		if (isSSL) {
			httpPrefix = "https://";
		}
		return httpPrefix + serverAddr + "/" + serverListManager.getContentPath() + relativePath;
	}

	private List<String> getSpasHeaders(List<String> paramValues) throws IOException {
		List<String> newHeaders = new ArrayList<>();
		// STS 临时凭证鉴权的优先级高于 AK/SK 鉴权
		if (STSConfig.getInstance().isSTSOn()) {
			STSCredential stsCredential = getSTSCredential();
			accessKey = stsCredential.getAccessKeyId();
			secretKey = stsCredential.getAccessKeySecret();
			newHeaders.add("Spas-SecurityToken");
			newHeaders.add(stsCredential.getSecurityToken());
		}

		if (StringUtils.isNotEmpty(accessKey) && StringUtils.isNotEmpty(secretKey)) {
			newHeaders.add("Spas-AccessKey");
			newHeaders.add(accessKey);

			List<String> signHeaders = SpasAdapter.getSignHeaders(paramValues, secretKey);
			if (signHeaders != null) {
				newHeaders.addAll(signHeaders);
			}
		}
		return newHeaders;
	}

	private STSCredential getSTSCredential() throws IOException {
		boolean cacheSecurityCredentials = STSConfig.getInstance().isCacheSecurityCredentials();
		if (cacheSecurityCredentials && sTSCredential != null) {
			long currentTime = System.currentTimeMillis();
			long expirationTime = sTSCredential.expiration.getTime();
			int timeToRefreshInMillisecond = STSConfig.getInstance().getTimeToRefreshInMillisecond();
			if (expirationTime - currentTime > timeToRefreshInMillisecond) {
				return sTSCredential;
			}
		}

		String stsResponse = getSTSResponse();
		this.sTSCredential = (STSCredential) JSONUtils.deserializeObject(stsResponse, new TypeReference<STSCredential>() {});
		log.info("code: {}, accessKeyId: {}, lastUpdated: {}, expiration: {}",
				sTSCredential.getCode(), sTSCredential.getAccessKeyId(), sTSCredential.getLastUpdated(), sTSCredential.getExpiration());
		return sTSCredential;
	}

	private static String getSTSResponse() throws IOException {
		String securityCredentials = STSConfig.getInstance().getSecurityCredentials();
		if (securityCredentials != null) {
			return securityCredentials;
		}

		String securityCredentialsUrl = STSConfig.getInstance().getSecurityCredentialsUrl();
		HttpURLConnection conn = null;
		int respCode;
		String response;
		try {
			conn = (HttpURLConnection) new URL(securityCredentialsUrl).openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(ParamUtil.getConnectTimeout() > 100 ? ParamUtil.getConnectTimeout() : 100);
			conn.setReadTimeout(1000);

			conn.connect();

			respCode = conn.getResponseCode();
			if (HttpURLConnection.HTTP_OK == respCode) {
				response = IoUtils.toString(conn.getInputStream(), Constants.ENCODE);
			}
			else {
				response = IoUtils.toString(conn.getErrorStream(), Constants.ENCODE);
			}
		}
		catch (IOException e) {
			log.error("can not get security credentials", e);
			throw e;
		}
		finally {
			if (conn != null) {
				conn.disconnect();
			}
		}

		if (HttpURLConnection.HTTP_OK == respCode) {
			return response;
		}
		log.error("can not get security credentials, securityCredentialsUrl: {}, response: {}",
				securityCredentialsUrl, response);
		throw new IOException("can not get security credentials, securityCredentialsUrl: " + securityCredentialsUrl + ", response: " + response);
	}

	@Data
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
	}
}

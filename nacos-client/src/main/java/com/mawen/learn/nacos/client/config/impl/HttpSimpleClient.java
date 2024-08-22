package com.mawen.learn.nacos.client.config.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.mawen.learn.nacos.api.exception.NacosException;
import com.mawen.learn.nacos.client.config.common.Constants;
import com.mawen.learn.nacos.client.config.utils.MD5;
import com.mawen.learn.nacos.client.utils.ParamUtil;
import com.mawen.learn.nacos.common.util.IoUtils;
import com.mawen.learn.nacos.common.util.UuidUtil;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/18
 */
public class HttpSimpleClient {

	public static HttpResult httpGet(String url, List<String> headers, List<String> paramValues,
			String encoding, long readTimeoutMS, boolean isSSL) throws IOException {
		String encodedContent = encodingParams(paramValues, encoding);
		url += (null == encodedContent) ? "" : "?" + encodedContent;
		if (Limiter.isLimit(MD5.getInstance().getMD5String(new StringBuilder(url).append(encodedContent).toString()))) {
			return new HttpResult(NacosException.CLIENT_OVER_THRESHOLD, "More than client-side current limit threshold");
		}

		HttpURLConnection conn = null;

		try {
			conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(ParamUtil.getConnectTimeout() > 100 ? ParamUtil.getConnectTimeout() : 100);
			conn.setReadTimeout((int) readTimeoutMS);

			List<String> newHeaders = getHeaders(url, headers, paramValues);
			setHeaders(conn, newHeaders, encoding);

			conn.connect();

			int respCode = conn.getResponseCode();
			String resp = null;

			if (HttpURLConnection.HTTP_OK == respCode) {
				resp = IoUtils.toString(conn.getInputStream(), encoding);
			}
			else {
				resp = IoUtils.toString(conn.getErrorStream(), encoding);
			}
			return new HttpResult(respCode, conn.getHeaderFields(), resp);
		}
		finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}

	public static HttpResult httpGet(String url, List<String> headers, List<String> paramValues, String encoding, long readTimeoutMs) throws IOException {
		return httpGet(url, headers, paramValues, encoding, readTimeoutMs, false);
	}

	public static HttpResult httpPost(String url, List<String> headers, List<String> paramValues, String encoding, long readTimeoutMs, boolean isSSL) throws IOException {
		String encodedContent = encodingParams(paramValues, encoding);
		if (Limiter.isLimit(MD5.getInstance().getMD5String(new StringBuilder(url).append(encodedContent).toString()))) {
			return new HttpResult(NacosException.CLIENT_OVER_THRESHOLD, "More than client-side current limit threshold");
		}

		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setRequestMethod("POST");
			conn.setConnectTimeout(ParamUtil.getConnectTimeout() > 3000 ? ParamUtil.getConnectTimeout() : 3000);
			conn.setReadTimeout((int) readTimeoutMs);
			conn.setDoOutput(true);
			conn.setDoInput(true);

			List<String> newHeaders = getHeaders(url, headers, paramValues);
			setHeaders(conn, newHeaders, encoding);

			conn.getOutputStream().write(encodedContent.getBytes(encoding));

			int respCode = conn.getResponseCode();
			String resp = null;

			if (HttpURLConnection.HTTP_OK == respCode) {
				resp = IoUtils.toString(conn.getInputStream(), encoding);
			}
			else {
				resp = IoUtils.toString(conn.getErrorStream(), encoding);
			}
			return new HttpResult(respCode, conn.getHeaderFields(), resp);
		}
		finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}

	public static HttpResult httpPost(String url, List<String> headers, List<String> paramValues, String encoding, long readTimeoutMs) throws IOException {
		return httpPost(url, headers, paramValues, encoding, readTimeoutMs, false);
	}

	public static HttpResult httpDelete(String url, List<String> headers, List<String> paramValues, String encoding, long readTimeoutMs, boolean isSSL) throws IOException {
		String encodedContent = encodingParams(paramValues, encoding);
		url += (null == encodedContent) ? "" : "?" + encodedContent;
		if (Limiter.isLimit(MD5.getInstance().getMD5String(new StringBuilder(url).append(encodedContent).toString()))) {
			return new HttpResult(NacosException.CLIENT_OVER_THRESHOLD, "More than client-side current limit threshold");
		}

		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setRequestMethod("DELETE");
			conn.setConnectTimeout(ParamUtil.getConnectTimeout() > 3000 ? ParamUtil.getConnectTimeout() : 3000);
			conn.setReadTimeout((int) readTimeoutMs);

			List<String> newHeaders = getHeaders(url, headers, paramValues);
			setHeaders(conn, newHeaders, encoding);

			conn.connect();

			int respCode = conn.getResponseCode();
			String resp = null;

			if (HttpURLConnection.HTTP_OK == respCode) {
				resp = IoUtils.toString(conn.getInputStream(), encoding);
			}
			else {
				resp = IoUtils.toString(conn.getErrorStream(), encoding);
			}
			return new HttpResult(respCode, conn.getHeaderFields(), resp);
		}
		finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}

	public static HttpResult httpDelete(String url, List<String> headers, List<String> paramValues, String encoding, long readTimeoutMs) throws IOException {
		return httpDelete(url, headers, paramValues, encoding, readTimeoutMs, false);
	}

	private static void setHeaders(HttpURLConnection conn, List<String> headers, String encoding) {
		if (null != headers) {
			for (Iterator<String> iter = headers.iterator(); iter.hasNext(); ) {
				conn.addRequestProperty(iter.next(), iter.next());
			}
		}

		conn.addRequestProperty("Client-Version", ParamUtil.getClientVersion());
		conn.addRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + encoding);

		String ts = String.valueOf(System.currentTimeMillis());
		String token = MD5.getInstance().getMD5String(ts + ParamUtil.getAppKey());

		conn.addRequestProperty(Constants.CLIENT_APPNAME_HEADER, ParamUtil.getAppName());
		conn.addRequestProperty(Constants.CLIENT_REQUEST_TS_HEADER, ts);
		conn.addRequestProperty(Constants.CLIENT_REQUEST_TOKEN_HEADER, token);
	}

	private static List<String> getHeaders(String url, List<String> headers, List<String> paramValues) {
		List<String> newHeaders = new ArrayList<>();
		newHeaders.add("exConfigInfo");
		newHeaders.add("true");
		newHeaders.add("RequestId");
		newHeaders.add(UuidUtil.generateUuid());
		if (headers != null) {
			newHeaders.addAll(headers);
		}
		return newHeaders;
	}

	private static String encodingParams(List<String> paramValues, String encoding) throws UnsupportedEncodingException {
		if (null == paramValues) {
			return null;
		}

		StringBuilder sb = new StringBuilder();

		for (Iterator<String> iter = paramValues.iterator(); iter.hasNext(); ) {
			sb.append(iter.next())
					.append("=")
					.append(URLEncoder.encode(iter.next(), encoding));
			if (iter.hasNext()) {
				sb.append("&");
			}
		}
		return sb.toString();
	}

	public static class HttpResult {

		public final int code;

		public final Map<String, List<String>> headers;

		public final String content;

		public HttpResult(int code, String content) {
			this(code, null, content);
		}

		public HttpResult(int code, Map<String, List<String>> headers, String content) {
			this.code = code;
			this.headers = headers;
			this.content = content;
		}
	}
}

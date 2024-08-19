package com.mawen.learn.nacos.client.config.impl;

import java.io.IOError;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.mawen.learn.nacos.api.exception.NacosException;
import com.mawen.learn.nacos.client.config.utils.MD5;
import com.mawen.learn.nacos.client.utils.ParamUtil;

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

		}
		finally {

		}
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
			this.code = code;
			this.content = content;
		}

		public HttpResult(int code, Map<String, List<String>> headers, String content) {
			this.code = code;
			this.headers = headers;
			this.content = content;
		}
	}
}

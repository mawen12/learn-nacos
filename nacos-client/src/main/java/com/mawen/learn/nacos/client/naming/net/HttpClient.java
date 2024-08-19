package com.mawen.learn.nacos.client.naming.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import com.google.common.net.HttpHeaders;
import com.mawen.learn.nacos.common.util.IoUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/19
 */
public class HttpClient {

	private static final Logger log = LoggerFactory.getLogger(HttpClient.class);

	public static final int TIME_OUT_MILLIS = Integer.parseInt(System.getProperty("com.mawen.vipserver.timeout", "50000"));
	public static final int CON_TIME_OUT_MILLIS = Integer.parseInt(System.getProperty("com.mawen.vipserver.ctimeout", "3000"));
	private static final boolean ENABLE_HTTPS = Boolean.parseBoolean(System.getProperty("tls.enable", "false"));

	static {
		// limit max redirection
		System.setProperty("http.maxRedirects", "5");
	}

	public static String getPrefix() {
		if (ENABLE_HTTPS) {
			return "https://";
		}
		return "http://";
	}

	public static HttpResult httpGet(String url, List<String> headers, Map<String, String> paramValues, String encoding) {
		return request(url, headers, paramValues, encoding, "GET");
	}

	public static HttpResult request(String url, List<String> headers, Map<String, String> paramValues, String encoding, String method) {
		HttpURLConnection conn = null;
		try {
			String encodedContent = encodingParams(paramValues, encoding);
			url += (null == encodedContent) ? "" : ("?" + encodedContent);

			conn = (HttpURLConnection) new URL(url).openConnection();

			conn.setConnectTimeout(CON_TIME_OUT_MILLIS);
			conn.setReadTimeout(TIME_OUT_MILLIS);
			conn.setRequestMethod(method);
			setHeaders(conn, headers, encoding);
			conn.connect();
			log.info("Request from server: {}", url);
			return getResult(conn);
		}
		catch (Exception e) {
			try {
				if (conn != null) {
					log.warn("failed to request {} from {}", conn.getURL(), InetAddress.getByName(conn.getURL().getHost()).getHostAddress());
				}
			}
			catch (Exception ex) {
				log.error("NA failed to request", ex);
			}

			log.error("NA failed to request", e);

			return new HttpResult(500, e.toString(), Collections.emptyMap());
		}
		finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}

	private static String encodingParams(Map<String, String> params, String encoding) throws UnsupportedEncodingException {
		StringBuilder sb = new StringBuilder();
		if (null == params || params.isEmpty()) {
			return null;
		}

		params.put("encoding", encoding);

		for (Map.Entry<String, String> entry : params.entrySet()) {
			if (StringUtils.isEmpty(entry.getValue())) {
				continue;
			}

			sb.append(entry.getKey()).append("=");
			sb.append(URLEncoder.encode(entry.getValue(), encoding));
			sb.append("&");
		}

		return sb.toString();
	}

	private static void setHeaders(HttpURLConnection conn, List<String> headers, String encoding) {
		if (null != headers) {
			for (Iterator<String> iter = headers.iterator(); iter.hasNext(); ) {
				conn.addRequestProperty(iter.next(), iter.next());
			}
		}

		conn.addRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + encoding);
		conn.addRequestProperty("Accept-Charset", encoding);
	}

	private static HttpResult getResult(HttpURLConnection conn) throws IOException {
		int respCode = conn.getResponseCode();

		InputStream inputStream;
		if (HttpURLConnection.HTTP_OK == respCode
				|| HttpURLConnection.HTTP_NOT_MODIFIED == respCode) {
			inputStream = conn.getInputStream();
		}
		else {
			inputStream = conn.getErrorStream();
		}

		Map<String, String> respHeaders = new HashMap<>(conn.getHeaderFields().size());
		for (Map.Entry<String, List<String>> entry : conn.getHeaderFields().entrySet()) {
			respHeaders.put(entry.getKey(), entry.getValue().get(0));
		}

		String encodingGzip = "gzip";

		if (encodingGzip.equals(respHeaders.get(HttpHeaders.CONTENT_ENCODING))) {
			inputStream = new GZIPInputStream(inputStream);
		}

		return new HttpResult(respCode, IoUtils.toString(inputStream, getCharset(conn)), respHeaders);
	}

	private static String getCharset(HttpURLConnection conn) {
		String contentType = conn.getContentType();
		if (StringUtils.isEmpty(contentType)) {
			return "UTF-8";
		}

		String[] values = contentType.split(";");
		if (values.length == 0) {
			return "UTF-8";
		}

		String charset = "UTF-8";
		for (String value : values) {
			value = value.trim();

			if (value.toLowerCase().startsWith("charset=")) {
				charset = value.substring("charset=".length());
			}
		}

		return charset;
	}

	public static class HttpResult {
		public final int code;
		public final String content;
		public final Map<String, String> respHeaders;

		public HttpResult(int code, String content, Map<String, String> respHeaders) {
			this.code = code;
			this.content = content;
			this.respHeaders = respHeaders;
		}

		public String getHeader(String name) {
			return respHeaders.get(name);
		}
	}
}

package com.mawen.learn.nacos.naming.misc;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import com.google.common.net.HttpHeaders;
import com.mawen.learn.nacos.common.util.IoUtils;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.FluentStringsMap;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/24
 */
@Slf4j
public class HttpClient {

	public static final int TIME_OUT_MILLIS = 10000;

	public static final int CON_TIME_OUT_MILLIS = 5000;

	private static AsyncHttpClient asyncHttpClient;

	static {
		AsyncHttpClientConfig.Builder builder = new AsyncHttpClientConfig.Builder();
		builder.setMaximumConnectionsTotal(-1);
		builder.setMaximumConnectionsPerHost(128);
		builder.setAllowPoolingConnection(true);
		builder.setFollowRedirects(false);
		builder.setIdleConnectionInPoolTimeoutInMs(TIME_OUT_MILLIS);
		builder.setConnectionTimeoutInMs(CON_TIME_OUT_MILLIS);
		builder.setCompressionEnabled(true);
		builder.setIOThreadMultiplier(1);
		builder.setMaxRequestRetry(0);
		builder.setUserAgent(UtilsAndCommons.SERVER_VERSION);

		asyncHttpClient = new AsyncHttpClient(builder.build());
	}

	public static HttpResult httpGet(String url, List<String> headers, Map<String, String> paramValues) {
		return httpGetWithTimeout(url, headers, paramValues, CON_TIME_OUT_MILLIS, TIME_OUT_MILLIS, "UTF-8");
	}

	public static HttpResult httpGetWithTimeout(String url, List<String> headers, Map<String, String> paramValues, int connectTimeout, int readTimeout) {
		return httpGetWithTimeout(url, headers, paramValues, connectTimeout, readTimeout, "UTF-8");
	}

	public static HttpResult httpGet(String url, List<String> headers, Map<String, String> paramValues, String encoding) {
		HttpURLConnection conn = null;
		try {
			String encodedContent = encodingParams(paramValues, encoding);
			url += (null == encodedContent) ? "" : "?" + encodedContent;

			conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setConnectTimeout(CON_TIME_OUT_MILLIS);
			conn.setReadTimeout(TIME_OUT_MILLIS);
			conn.setRequestMethod("GET");
			setHeaders(conn, headers, encoding);
			conn.connect();

			return getResult(conn);
		}
		catch (Exception e) {
			log.warn("Exception while request: {}, caused: {}", url, e.getMessage(), e);
			return new HttpResult(500, e.toString(), Collections.emptyMap());
		}
		finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}

	public static HttpResult httpGetWithTimeout(String url, List<String> headers, Map<String, String> paramValues, int connectTimeout, int readTimeout, String encoding) {
		HttpURLConnection conn = null;
		try {
			String encodedContent = encodingParams(paramValues, encoding);
			url += (null == encodedContent) ? "" : "?" + encodedContent;

			conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setConnectTimeout(connectTimeout);
			conn.setReadTimeout(readTimeout);
			conn.setRequestMethod("GET");

			conn.addRequestProperty("Client-Version", UtilsAndCommons.SERVER_VERSION);
			setHeaders(conn, headers, encoding);
			conn.connect();

			return getResult(conn);
		}
		catch (Exception e) {
			log.warn("Exception while request: {}, caused: {}", url, e.getMessage(), e);
			return new HttpResult(500, e.toString(), Collections.emptyMap());
		}
		finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}

	public static void asyncHttpGet(String url, List<String> headers, Map<String, String> paramValues, AsyncCompletionHandler handler) throws IOException {
		if (MapUtils.isEmpty(paramValues)) {
			String encodedContent = encodingParams(paramValues, "UTF-8");
			url += null == encodedContent ? "" : "?" + encodedContent;
		}

		AsyncHttpClient.BoundRequestBuilder builder = asyncHttpClient.prepareGet(url);

		if (!CollectionUtils.isEmpty(headers)) {
			for (String header : headers) {
				String[] arr = header.split("=");
				builder.setHeader(arr[0], arr[1]);
			}
		}
		builder.setHeader("Accept-Charset", "UTF-8");

		if (handler != null) {
			builder.execute(handler);
		}
		else {
			builder.execute();
		}
	}

	public static void asyncHttpPostLarge(String url, List<String> headers, String content, AsyncCompletionHandler handler) throws IOException {
		asyncHttpPostLarge(url, headers, content.getBytes("UTF-8"), handler);
	}

	public static void asyncHttpPostLarge(String url, List<String> headers, byte[] content, AsyncCompletionHandler handler) throws IOException {
		AsyncHttpClient.BoundRequestBuilder builder = asyncHttpClient.preparePost(url);

		if (!CollectionUtils.isEmpty(headers)) {
			for (String header : headers) {
				String[] arr = header.split("=");
				builder.setHeader(arr[0], arr[1]);
			}
		}

		builder.setBody(content);
		builder.setHeader("Content-Type", "application/json;charset=UTF-8");
		builder.setHeader("Accept-Charset", "UTF-8");
		builder.setHeader("Accept-Encoding", "gzip");
		builder.setHeader("Content-Encoding", "gzip");

		if (handler != null) {
			builder.execute(handler);
		}
		else {
			builder.execute();
		}
	}

	public static void asyncHttpPost(String url, List<String> headers, Map<String, String> paramValues, AsyncCompletionHandler handler) throws IOException {
		AsyncHttpClient.BoundRequestBuilder builder = asyncHttpClient.preparePost(url);

		if (!CollectionUtils.isEmpty(headers)) {
			for (String header : headers) {
				String[] arr = header.split("=");
				builder.setHeader(arr[0], arr[1]);
			}
		}

		if (!MapUtils.isEmpty(paramValues)) {
			FluentStringsMap params = new FluentStringsMap();
			for (Map.Entry<String, String> entry : paramValues.entrySet()) {
				params.put(entry.getKey(), Collections.singletonList(entry.getValue()));
			}
			builder.setParameters(params);
		}

		builder.setHeader("Accept-Charset", "UTF-8");

		if (handler != null) {
			builder.execute(handler);
		}
		else {
			builder.execute();
		}
	}

	public static HttpResult httpPost(String url, List<String> headers, Map<String, String> paramValues) {
		return httpPost(url, headers, paramValues, "UTF-8");
	}

	public static HttpResult httpPost(String url, List<String> headers, Map<String, String> paramValues, String encoding) {
		try {
			HttpClientBuilder builder = HttpClients.custom();
			builder.setUserAgent(UtilsAndCommons.SERVER_VERSION);
			builder.setConnectionTimeToLive(CON_TIME_OUT_MILLIS, TimeUnit.MILLISECONDS);

			CloseableHttpClient httpClient = builder.build();
			HttpPost httpPost = new HttpPost(url);

			RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(5000).setConnectTimeout(5000).setSocketTimeout(5000)
					.setRedirectsEnabled(true).setMaxRedirects(5)
					.build();
			httpPost.setConfig(requestConfig);

			List<NameValuePair> nvps = new ArrayList<>();

			for (Map.Entry<String, String> entry : paramValues.entrySet()) {
				nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}

			httpPost.setEntity(new UrlEncodedFormEntity(nvps, encoding));
			CloseableHttpResponse response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();

			String charset = encoding;
			if (entity.getContentType() != null) {
				HeaderElement[] headerElements = entity.getContentType().getElements();

				if (headerElements != null && headerElements.length > 0 && headerElements[0] != null &&
						headerElements[0].getParameterByName("charset") != null) {
					charset = headerElements[0].getParameterByName("charset").getValue();
				}
			}

			return new HttpResult(response.getStatusLine().getStatusCode(), IoUtils.toString(entity.getContent(), charset), Collections.emptyMap());
		}
		catch (Throwable e) {
			return new HttpResult(500, e.toString(), Collections.emptyMap());
		}
	}

	public static HttpResult httpPostLarge(String url, Map<String, String> headers, String content) {
		try {
			HttpClientBuilder builder = HttpClients.custom();
			builder.setUserAgent(UtilsAndCommons.SERVER_VERSION);
			builder.setConnectionTimeToLive(500, TimeUnit.MILLISECONDS);

			CloseableHttpClient httpClient = builder.build();
			HttpPost httpPost = new HttpPost(url);

			for (Map.Entry<String, String> entry : headers.entrySet()) {
				httpPost.setHeader(entry.getKey(), entry.getValue());
			}

			httpPost.setEntity(new StringEntity(content, ContentType.create("application/json", "UTF-8")));
			HttpResponse response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();

			HeaderElement[] headerElements = entity.getContentType().getElements();
			String charset = headerElements[0].getParameterByName("charset").getValue();

			return new HttpResult(response.getStatusLine().getStatusCode(), IoUtils.toString(entity.getContent(), charset), Collections.emptyMap());
		}
		catch (Exception e) {
			return new HttpResult(500, e.toString(), Collections.emptyMap());
		}
	}

	public static String encodingParams(Map<String, String> params, String encoding) throws UnsupportedEncodingException {
		StringBuilder sb = new StringBuilder();
		if (null == params || params.isEmpty()) {
			return null;
		}

		params.put("encoding", encoding);
		params.put("nofix", "1");

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
			for (Iterator<String> iter = headers.iterator(); iter.hasNext();) {
				conn.setRequestProperty(iter.next(), iter.next());
			}
		}

		conn.addRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + encoding);
		conn.addRequestProperty("Accept-Charset", encoding);
		conn.addRequestProperty("Client-Version", UtilsAndCommons.SERVER_VERSION);
	}

	private static String getCharset(HttpURLConnection conn) {
		String contentType = conn.getContentType();
		if (StringUtils.isEmpty(contentType)) {
			return "utf-8";
		}

		String[] values = contentType.split(";");
		if (values.length == 0) {
			return "utf-8";
		}

		String charset = "utf-8";
		for (String value : values) {
			value = value.trim();

			if (value.toLowerCase().startsWith("charset=")) {
				charset = value.substring("charset=".length());
			}
		}

		return charset;
	}

	private static HttpResult getResult(HttpURLConnection conn) throws IOException {
		int respCode = conn.getResponseCode();

		InputStream inputStream;
		if (HttpURLConnection.HTTP_OK == respCode) {
			inputStream = conn.getInputStream();
		}
		else {
			inputStream = conn.getErrorStream();
		}

		Map<String, String> respHeaders = new HashMap<>(conn.getHeaderFields().size());
		for (Map.Entry<String, List<String>> entry : conn.getHeaderFields().entrySet()) {
			respHeaders.put(entry.getKey(), entry.getValue().get(0));
		}

		String gzipEncoding = "gzip";
		if (gzipEncoding.equals(respHeaders.get(HttpHeaders.CONTENT_ENCODING))) {
			inputStream = new GZIPInputStream(inputStream);
		}

		HttpResult result = new HttpResult(respCode, IoUtils.toString(inputStream, getCharset(conn)), respHeaders);
		inputStream.close();

		return result;


		// TODO start here
	}

	@AllArgsConstructor
	public static class HttpResult {
		public final int code;

		public final String content;

		private final Map<String, String> respHeaders;

		public String getHeader(String name) {
			return respHeaders.get(name);
		}
	}
}

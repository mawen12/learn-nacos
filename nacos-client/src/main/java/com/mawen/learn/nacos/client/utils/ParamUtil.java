package com.mawen.learn.nacos.client.utils;

import java.io.InputStream;
import java.util.Properties;

import com.mawen.learn.nacos.client.config.impl.HttpSimpleClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/15
 */
public class ParamUtil {

	private static final Logger log = LoggerFactory.getLogger(ParamUtil.class);

	private static String defaultContextPath = "nacos";
	private static String defaultNodesPath = "serverList";
	private static String appKey;
	private static String appName;
	private static String defaultServerPort;
	private static String clientVersion = "unknown";
	private static int connectTimeout;
	private static double perTaskConfigSize = 3000;

	static {
		// 客户端身份信息
		appKey = System.getProperty("nacos.client.appKey", "");

		appName = AppNameUtils.getAppName();

		String defaultServerPortImpl = "8080";

		defaultServerPort = System.getProperty("nacos.server.port", defaultServerPortImpl);
		log.info("settings [req-serv] nacos-server port:{}", defaultServerPort);

		String tmp = "1000";
		try {
			tmp = System.getProperty("NACOS.CONNECT.TIMEOUT", "1000");
			connectTimeout = Integer.parseInt(tmp);
		}
		catch (NumberFormatException e) {
			final String msg = "[http-client] invalid connect timeout: " + tmp;
			log.error("settings NACOS-XXXXX " + msg, e);
			throw new IllegalArgumentException(msg, e);
		}
		log.info("settings [http-client] connect timeout:{}", connectTimeout);

		try {
			InputStream in = HttpSimpleClient.class.getClassLoader().getResourceAsStream("application.properties");
			Properties props = new Properties();
			props.load(in);
			String val = null;
			val = props.getProperty("version");
			if (val != null) {
				clientVersion = val;
			}
			log.info("NACOS_CLIENT_VERSION:{}", clientVersion);
		}
		catch (Exception e) {
			log.error("500 read application.properties", e);
		}

		try {
			perTaskConfigSize = Double.valueOf(System.getProperty("PER_TASK_CONFIG_SIZE", "3000"));
			log.warn("PER_TASK_CONFIG_SIZE:{}", perTaskConfigSize);
		}
		catch (Throwable e) {
			log.error("PER_TASK_CONFIG_SIZE invalid", e);
		}
	}

	public static String getDefaultContextPath() {
		return defaultContextPath;
	}

	public static void setDefaultContextPath(String defaultContextPath) {
		ParamUtil.defaultContextPath = defaultContextPath;
	}

	public static String getDefaultNodesPath() {
		return defaultNodesPath;
	}

	public static void setDefaultNodesPath(String defaultNodesPath) {
		ParamUtil.defaultNodesPath = defaultNodesPath;
	}

	public static String getAppKey() {
		return appKey;
	}

	public static void setAppKey(String appKey) {
		ParamUtil.appKey = appKey;
	}

	public static String getAppName() {
		return appName;
	}

	public static void setAppName(String appName) {
		ParamUtil.appName = appName;
	}

	public static String getDefaultServerPort() {
		return defaultServerPort;
	}

	public static void setDefaultServerPort(String defaultServerPort) {
		ParamUtil.defaultServerPort = defaultServerPort;
	}

	public static String getClientVersion() {
		return clientVersion;
	}

	public static void setClientVersion(String clientVersion) {
		ParamUtil.clientVersion = clientVersion;
	}

	public static int getConnectTimeout() {
		return connectTimeout;
	}

	public static void setConnectTimeout(int connectTimeout) {
		ParamUtil.connectTimeout = connectTimeout;
	}

	public static double getPerTaskConfigSize() {
		return perTaskConfigSize;
	}

	public static void setPerTaskConfigSize(double perTaskConfigSize) {
		ParamUtil.perTaskConfigSize = perTaskConfigSize;
	}
}

package com.mawen.learn.nacos.client.naming.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/19
 */
public class NetUtils {

	private static String LOCAL_IP;

	public static String localIP() {
		try {
			if (!StringUtils.isEmpty(LOCAL_IP)) {
				return LOCAL_IP;
			}

			String ip = System.getProperty("com.mawen.vipserver.localIP", InetAddress.getLocalHost().getHostAddress());
			return LOCAL_IP = ip;
		}
		catch (UnknownHostException e) {
			return "resolve_failed";
		}
	}
}

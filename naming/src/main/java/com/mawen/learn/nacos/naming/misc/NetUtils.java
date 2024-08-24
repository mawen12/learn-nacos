package com.mawen.learn.nacos.naming.misc;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.mawen.learn.nacos.naming.boot.RunningConfig;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/24
 */
public class NetUtils {

	public static String localIp() {
		try {
			return InetAddress.getLocalHost().getHostAddress() + ":" + RunningConfig.getServerPort();
		}
		catch (UnknownHostException e) {
			return "resolve_failed";
		}
	}

	public static String num2ip(int ip) {
		int[] b = new int[4];

		b[0] = (int) ((ip >> 24) & 0xFF);
		b[1] = (int) ((ip >> 16) & 0xFF);
		b[2] = (int) ((ip >> 8) & 0xFF);
		b[3] = (int) (ip & 0xFF);

		return Integer.toString(b[0]) + "." + Integer.toString(b[1]) + "." + Integer.toString(b[2]) + "." + Integer.toString(b[3]);
	}
}

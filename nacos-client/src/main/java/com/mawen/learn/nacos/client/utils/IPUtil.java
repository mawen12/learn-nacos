package com.mawen.learn.nacos.client.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/21
 */
public class IPUtil {

	public static boolean isIPV4(String addr) {
		if (null == addr) {
			return false;
		}

		String regexp = "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$";

		Pattern pat = Pattern.compile(regexp);

		Matcher mat = pat.matcher(addr);
		return mat.find();
	}

	public static boolean isIPV6(String addr) {
		if (null == addr) {
			return false;
		}

		String rexp = "^([\\da-fA-F]{1,4}:){7}[\\da-fA-F]{1,4}$";

		Pattern pat = Pattern.compile(rexp);

		Matcher mat = pat.matcher(addr);
		return mat.find();
	}
}

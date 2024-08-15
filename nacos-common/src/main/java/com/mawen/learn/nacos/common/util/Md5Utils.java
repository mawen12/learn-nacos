package com.mawen.learn.nacos.common.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/15
 */
public class Md5Utils {

	private static char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

	private static final int HEX_VALUE_COUNT = HEX_DIGITS.length;

	private static final String MD5 = "MD5";

	public static String getMD5(String value, String encode) {
		String result = "";
		try {
			result = getMD5(value.getBytes(encode));
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static String getMD5(byte[] bytes) {
		char[] str = new char[16 * 2];
		try {
			MessageDigest md = MessageDigest.getInstance(MD5);
			md.update(bytes);
			byte[] temp = md.digest();
			int k = 0;
			for (int i = 0; i < HEX_VALUE_COUNT; i++) {
				byte byte0 = temp[i];
				str[k++] = HEX_DIGITS[byte0 >>> 4 & 0xf];
				str[k++] = HEX_DIGITS[byte0 & 0xf];
			}
		}
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return new String(str);
	}
}

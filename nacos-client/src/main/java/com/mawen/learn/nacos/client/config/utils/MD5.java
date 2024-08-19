package com.mawen.learn.nacos.client.config.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import com.mawen.learn.nacos.client.config.common.Constants;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/18
 */
public class MD5 {

	private static int DIGITS_SIZE = 16;
	private static char[] digits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

	private static Map<Character, Integer> rDigits = new HashMap<>(DIGITS_SIZE);

	static {
		for (int i = 0; i < digits.length; i++) {
			rDigits.put(digits[i], i);
		}
	}

	private static MD5 me = new MD5();
	private MessageDigest mHasher;
	private ReentrantLock opLock = new ReentrantLock();

	private MD5() {
		try {
			mHasher = MessageDigest.getInstance("md5");
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static MD5 getInstance() {
		return me;
	}

	public String getMD5String(String content) {
		return bytes2String(hash(content));
	}

	public byte[] hash(String str) {
		opLock.lock();
		try {
			byte[] bt = mHasher.digest(str.getBytes(Constants.ENCODE));
			if (bt == null || bt.length != DIGITS_SIZE) {
				throw new IllegalArgumentException("md5 need");
			}
			return bt;
		}
		catch (UnsupportedEncodingException e) {
			throw new RuntimeException("unsupported utf-8 encoding", e);
		}
		finally {
			opLock.unlock();
		}
	}

	public byte[] hash(byte[] data) {
		opLock.lock();
		try {
			byte[] bt = mHasher.digest(data);
			if (bt == null || bt.length != DIGITS_SIZE) {
				throw new IllegalArgumentException("md5 need");
			}
			return bt;
		}
		finally {
			opLock.unlock();
		}
	}

	public String bytes2String(byte[] bytes) {
		int l = bytes.length;

		char[] out = new char[l << 1];

		for (int i = 0, j = 0; i < l; i++) {
			out[j++] = digits[(0xF0 & bytes[i]) >>> 4];
			out[j++] = digits[0x0F & bytes[i]];
		}

		return new String(out);
	}
}

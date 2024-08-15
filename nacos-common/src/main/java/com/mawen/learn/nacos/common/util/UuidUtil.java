package com.mawen.learn.nacos.common.util;

import java.util.UUID;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/15
 */
public class UuidUtil {

	public static String generateUuid() {
		return UUID.randomUUID().toString();
	}
}

package com.mawen.learn.nacos.client.naming.utils;

import java.util.Collection;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/19
 */
public class CollectionUtils {

	private static Integer INTEGER_ONE = 1;

	public CollectionUtils(){}

	public static boolean isEmpty(Collection<?> collection) {
		return collection == null || collection.isEmpty();
	}
}

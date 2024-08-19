package com.mawen.learn.nacos.client.naming.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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

	public static boolean isEqualCollection(final Collection a, final Collection b) {
		if (a.size() != b.size()) {
			return false;
		}
		else {
			Map mapa = getCardinalityMap(a);
			Map mapb = getCardinalityMap(b);
			if (mapa.size() != mapb.size()) {
				return false;
			}
			else {
				Iterator it = mapa.keySet().iterator();
				while (it.hasNext()) {
					Object obj = it.next();
					if (getFreq(obj, mapa) != getFreq(obj, mapb)) {
						return false;
					}
				}
				return true;
			}
		}
	}

	public static Map getCardinalityMap(final Collection coll) {
		Map count = new HashMap(coll.size());
		for (Iterator it = coll.iterator(); it.hasNext(); ) {
			Object obj = it.next();
			Integer c = (Integer) (count.get(obj));
			if (c == null) {
				count.put(obj, INTEGER_ONE);
			}
			else {
				count.put(obj, c + INTEGER_ONE);
			}
		}
		return count;
	}

	private static int getFreq(final Object obj, final Map freeMap) {
		Integer count = (Integer) freeMap.get(obj);
		if (count != null) {
			return count.intValue();
		}
		return 0;
	}
}

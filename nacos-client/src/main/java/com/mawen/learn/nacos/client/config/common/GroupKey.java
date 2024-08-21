package com.mawen.learn.nacos.client.config.common;

import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/21
 */
public class GroupKey {

	public static String getKey(String dataId, String group) {
		StringBuilder sb = new StringBuilder();
		urlEncode(dataId, sb);
		sb.append('+');
		urlEncode(group, sb);
		return sb.toString();
	}

	public static String getKeyTenant(String dataId, String group, String tenant) {
		StringBuilder sb = new StringBuilder();
		urlEncode(dataId, sb);
		sb.append('+');
		urlEncode(group, sb);

		if (StringUtils.isNotEmpty(tenant)) {
			sb.append('+');
			urlEncode(tenant, sb);
		}

		return sb.toString();
	}

	public static String getKey(String dataId, String group, String datumStr) {
		StringBuilder sb = new StringBuilder();
		urlEncode(dataId, sb);
		sb.append('+');
		urlEncode(group, sb);
		sb.append('+');
		urlEncode(datumStr, sb);
		return sb.toString();
	}

	public static String[] parseKey(String groupKey) {
		StringBuilder sb = new StringBuilder();
		String dataId = null, group = null, tenant = null;

		for (int i = 0; i < groupKey.length(); i++) {
			char c = groupKey.charAt(i);
			if ('+' == c) {
				if (null == dataId) {
					dataId = sb.toString();
					sb.setLength(0);
				}
				else if (null == group) {
					group = sb.toString();
					sb.setLength(0);
				}
				else {
					throw new IllegalArgumentException("invalid groupKey " + groupKey);
				}
			}
			else if ('%' == c) {
				char next = groupKey.charAt(++i);
				char nextnext = groupKey.charAt(++i);
				if ('2' == next && 'B' == nextnext) {
					sb.append('+');
				}
				else if ('2' == next && '5' == nextnext) {
					sb.append('%');
				}
				else {
					throw new IllegalArgumentException("invalid groupKey " + groupKey);
				}
			}
			else {
				sb.append(c);
			}
		}

		if (StringUtils.isBlank(group)) {
			group = sb.toString();
			if (group.length() == 0) {
				throw new IllegalArgumentException("invalid groupKey " + groupKey);
			}
		}
		else {
			tenant = sb.toString();
			if (tenant.length() == 0) {
				throw new IllegalArgumentException("invalid groupKey " + groupKey);
			}
		}

		return new String[]{dataId, group, tenant};
	}

	/**
	 * + -> %2B
	 * % -> %25
	 */
	static void urlEncode(String str, StringBuilder sb) {
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if ('+' == c) {
				sb.append("%2B");
			}
			else if ('%' == c) {
				sb.append("%25");
			}
			else {
				sb.append(c);
			}
		}
	}
}

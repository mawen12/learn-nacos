package com.mawen.learn.nacos.client.config.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/21
 */
public class TenantUtil {

	private static String userTenant = "";

	static {
		userTenant = System.getProperty("tenant.id", "");
		if (StringUtils.isBlank(userTenant)) {
			userTenant = System.getProperty("acm.namespace", "");
		}
	}

	public static String getUserTenant() {
		return userTenant;
	}

	public static void setUserTenant(String userTenant) {
		TenantUtil.userTenant = userTenant;
	}
}

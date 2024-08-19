package com.mawen.learn.nacos.client.utils;

import java.io.File;

import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/18
 */
public class AppNameUtils {

	private static final String PARAM_MARKING_PROJECT = "project.name";
	private static final String PARAM_MARKING_JBOSS = "jboss.server.home.dir";
	private static final String PARAM_MARKING_JETTY = "jetty.home";
	private static final String PARAM_MARKING_TOMCAT = "catalina.home";

	private static final String LINUX_ADMIN_HOME = "/home/admin/";
	private static final String SERVER_JBOSS = "jboss";
	private static final String SERVER_JETTY = "jetty";
	private static final String SERVER_TOMCAT = "tomcat";
	private static final String SERVER_UNKNOWN = "unknown server";

	public static String getAppName() {
		String appName = null;

		appName = getAppNameByProjectName();
		if (appName != null) {
			return appName;
		}

		appName = getAppNameByServerName();
		if (appName != null) {
			return appName;
		}

		return "unknown";
	}

	private static String getAppNameByProjectName() {
		return System.getProperty(PARAM_MARKING_PROJECT);
	}

	private static String getAppNameByServerName() {
		String serverHome = null;
		if (SERVER_JBOSS.equals(getServerType())) {
			serverHome = System.getProperty(PARAM_MARKING_JBOSS);
		}
		else if (SERVER_JETTY.equals(getServerType())) {
			serverHome = System.getProperty(PARAM_MARKING_JETTY);
		}
		else if (SERVER_TOMCAT.equals(getServerType())) {
			serverHome = System.getProperty(PARAM_MARKING_TOMCAT);
		}

		if (serverHome != null && serverHome.startsWith(LINUX_ADMIN_HOME)) {
			return StringUtils.substringBetween(serverHome, LINUX_ADMIN_HOME, File.separator);
		}

		return null;
	}

	private static String getServerType() {
		String serverType = null;
		if (System.getProperty(PARAM_MARKING_JBOSS) != null) {
			serverType = SERVER_JBOSS;
		}
		else if (System.getProperty(PARAM_MARKING_JETTY) != null) {
			serverType = SERVER_JETTY;
		}
		else if (System.getProperty(PARAM_MARKING_TOMCAT) != null) {
			serverType = SERVER_TOMCAT;
		}
		else {
			serverType = SERVER_UNKNOWN;
		}
		return serverType;
	}
}

package com.mawen.learn.nacos.client.config.utils;

import jdk.jfr.internal.JVM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/21
 */
public class JVMUtil {

	private static final Logger log = LoggerFactory.getLogger(JVMUtil.class);

	private static final String TRUE = "true";

	private static Boolean isMultiInstance = false;

	static {
		String multiDeploy = System.getProperty("isMultiInstance", "false");
		if (TRUE.equals(multiDeploy)) {
			isMultiInstance = true;
		}
		log.info("isMultiInstance: {}", isMultiInstance);
	}

	public static Boolean isMultiInstance() {
		return isMultiInstance;
	}
}

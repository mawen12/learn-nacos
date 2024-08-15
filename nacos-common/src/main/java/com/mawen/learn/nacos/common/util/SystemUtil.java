package com.mawen.learn.nacos.common.util;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sun.management.OperatingSystemMXBean;
import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/15
 */
public class SystemUtil {

	private static OperatingSystemMXBean operatingSystemMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

	public static List<String> getIpsBySystemEnv(String key) {
		String env = getSystemEnv(key);
		List<String> ips = new ArrayList<>();
		if (StringUtils.isNotEmpty(env)) {
			ips = Arrays.asList(env.split(","));
		}
		return ips;
	}

	public static String getSystemEnv(String key) {
		return System.getenv(key);
	}

	public static float getLoad() {
		return (float) operatingSystemMXBean.getSystemLoadAverage();
	}

	public static float getCPU() {
		return (float) operatingSystemMXBean.getSystemCpuLoad();
	}

	public static float getMem() {
		return (float) operatingSystemMXBean.getFreePhysicalMemorySize();
	}
}

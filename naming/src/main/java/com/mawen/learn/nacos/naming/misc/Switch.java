package com.mawen.learn.nacos.naming.misc;

import lombok.extern.slf4j.Slf4j;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/23
 */
@Slf4j
public class Switch {

	private static volatile SwitchDomain dom = new SwitchDomain();

	private static boolean enableService = false;

	public static long getClientBeatInterval() {
		return dom.getClientBeatInterval();
	}

	public static void setClientBeatInterval(long clientBeatInterval) {
		dom.setClientBeatInterval(clientBeatInterval);
	}

	static {
		log.info("switch init start!");

		RafeCore
	}

}

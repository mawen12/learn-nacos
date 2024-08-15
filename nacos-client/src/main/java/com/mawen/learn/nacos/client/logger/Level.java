package com.mawen.learn.nacos.client.logger;

/**
 * 日志级别
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/15
 */
public enum Level {
	DEBUG("DEBUG"),
	INFO("INFO"),
	WARN("WARN"),
	ERROR("ERROR"),
	OFF("OFF");

	private final String name;

	Level(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public static Level codeOf(String level) {
		for (Level l : Level.values()) {
			if (l.name.equals(level)) {
				return l;
			}
		}

		return OFF;
	}
}

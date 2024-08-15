package com.mawen.learn.nacos.api.config.listener;

import java.util.concurrent.Executor;

/**
 * Listener to watch config
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/15
 */
public interface Listener {

	/**
	 * Executor to execute the receive
	 */
	Executor getExecutor();

	/**
	 * 接受配置信息
	 */
	void receiveConfigInfo(final String configInfo);
}

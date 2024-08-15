package com.mawen.learn.nacos.api.config;

import com.mawen.learn.nacos.api.config.listener.Listener;
import com.mawen.learn.nacos.api.exception.NacosException;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/15
 */
public interface ConfigService {

	String getConfig(String dataId, String group, long timeoutMs) throws NacosException;

	boolean pushConfig(String dataId, String group, String content) throws NacosException;

	boolean removeConfig(String dataId, String group) throws NacosException;

	/**
	 * Add a listener to the configuration, after the server to modify the configuration,
	 * the client will use the incoming listener callback.
	 *
	 * Recommended asynchronous processing, the application can implement the getExecutor method in the
	 * ManagerListener, provide a thread pool of execution.
	 *
	 * If provided, use the main thread callback. May block other configurations or be blocked
	 * by other configurations.
	 *
	 * @param dataId Config ID
	 * @param group Config Group
	 * @param listener listener
	 * @throws NacosException NacosException
	 */
	void addListener(String dataId, String group, Listener listener) throws NacosException;

	void removeListener(String dataId, String group, Listener listener) throws NacosException;
}

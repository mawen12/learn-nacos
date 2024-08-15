package com.mawen.learn.nacos.api.config.listener;

import java.util.concurrent.Executor;

/**
 * 共享监听器
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/15
 */
public abstract class AbstractSharedListener implements Listener{

	private volatile String dataId;

	private volatile String group;

	public final void fillContext(String dataId, String group) {
		this.dataId = dataId;
		this.group = group;
	}

	@Override
	public Executor getExecutor() {
		return null;
	}

	@Override
	public void receiveConfigInfo(String configInfo) {
		innerReceive(dataId, group, configInfo);
	}

	public abstract void innerReceive(String dataId, String group, String configInfo);
}

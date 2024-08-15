package com.mawen.learn.nacos.api.config.listener;

import java.util.concurrent.Executor;

/**
 * 监听适配器，使用默认通知县城
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/15
 */
public abstract class AbstractListener implements Listener{

	@Override
	public Executor getExecutor() {
		return null;
	}
}

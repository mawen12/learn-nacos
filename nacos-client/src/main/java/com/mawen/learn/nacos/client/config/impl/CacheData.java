package com.mawen.learn.nacos.client.config.impl;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import com.mawen.learn.nacos.api.config.listener.AbstractListener;
import com.mawen.learn.nacos.api.config.listener.AbstractSharedListener;
import com.mawen.learn.nacos.api.config.listener.Listener;
import com.mawen.learn.nacos.api.exception.NacosException;
import com.mawen.learn.nacos.client.config.filter.impl.ConfigFilterChainManager;
import com.mawen.learn.nacos.client.config.filter.impl.ConfigResponse;
import com.mawen.learn.nacos.client.config.utils.MD5;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/21
 */
public class CacheData {

	private static final Logger log = LoggerFactory.getLogger(CacheData.class);

	private final String name;
	private final ConfigFilterChainManager configFilterChainManager;
	private final String dataId;
	private final String group;
	private final String tenant;
	private final CopyOnWriteArrayList<ManagerListenerWrap> listeners;

	private volatile String md5;

	private volatile boolean isUseLocalConfig = false;

	private volatile long localConfigLastModified;

	private volatile String content;

	private int taskId;

	private volatile boolean isInitializing = false;

	public boolean isInitializing() {
		return isInitializing;
	}

	public String getMd5() {
		return md5;
	}

	public String getTenant() {
		return tenant;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
		this.md5 = getMd5String(content);
	}

	public long getLocalConfigLastModified() {
		return localConfigLastModified;
	}

	public void setLocalConfigLastModified(long localConfigLastModified) {
		this.localConfigLastModified = localConfigLastModified;
	}

	public boolean isUseLocalConfigInfo() {
		return isUseLocalConfig;
	}

	public void setUseLocalConfigInfo(boolean useLocalConfigInfo) {
		isUseLocalConfig = useLocalConfigInfo;
		if (!useLocalConfigInfo) {
			localConfigLastModified = -1;
		}
	}

	public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	public void addListener(Listener listener) {
		if (null == listener) {
			throw new IllegalArgumentException("listener must not be null");
		}

		ManagerListenerWrap wrap = new ManagerListenerWrap(listener);

		if (listeners.addIfAbsent(wrap)) {
			log.info("[add-listener] ok, tenant = {}, dataId = {}, group = {}, cnt = {}", tenant, dataId, group, listeners.size());
		}
	}

	public void removeListener(Listener listener) {
		if (null == listener) {
			throw new IllegalArgumentException("listener must not be null");
		}

		ManagerListenerWrap wrap = new ManagerListenerWrap(listener);
		if (listeners.remove(wrap)) {
			log.info("[remove-listener] ok, dataId = {}, group = {}, cnt = {}", dataId, group, listeners.size());
		}
	}

	public List<Listener> getListeners() {
		return listeners.stream().map(wrap -> wrap.listener).collect(Collectors.toList());
	}

	@Override
	public int hashCode() {
		return Objects.hash(dataId, group);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CacheData cacheData = (CacheData) o;
		return Objects.equals(dataId, cacheData.dataId) && Objects.equals(group, cacheData.group);
	}

	@Override
	public String toString() {
		return "CacheData{" +
				"dataId='" + dataId + '\'' +
				", group='" + group + '\'' +
				'}';
	}

	void checkListenerMd5() {
		for (ManagerListenerWrap wrap : listeners) {
			if (!md5.equals(wrap.lastCallMd5)) {
				safeNotifyListener(dataId, group, content, md5, wrap);
			}
		}
	}

	private void safeNotifyListener(final String dataId, final String group, final String content, final String md5, final ManagerListenerWrap listenerWrap) {
		final Listener listener = listenerWrap.listener;

		Runnable job = () -> {
			ClassLoader myClassLoader = Thread.currentThread().getContextClassLoader();
			ClassLoader appClassLoader = listener.getClass().getClassLoader();

			try {
				if (listener instanceof AbstractSharedListener) {
					AbstractListener adapter = (AbstractListener) listener;
					adapter.fillContext(dataId, group);
					log.info("[notify-context] dataId = {}, group = {}, md5 = {}", dataId, group, md5);
				}

				// 执行回调之前先将线程classloader设置为具体webapp的classloader，以免回调方法中调用的spi接口是出现异常或错用
				Thread.currentThread().setContextClassLoader(appClassLoader);

				ConfigResponse cr = new ConfigResponse();
				cr.setDataId(dataId);
				cr.setGroup(group);
				cr.setContent(content);
				configFilterChainManager.doFilter(null, cr);
				String contentTmp = cr.getContent();
				listener.receiveConfigInfo(contentTmp);
				listenerWrap.lastCallMd5 = md5;
				log.info("[notify-ok] dataId = {}, group = {}, md5 = {}, listener = {}", dataId, group, md5, listener);
			}
			catch (NacosException e) {
				// TODO start here
			}
		};
	}

	private static String getMd5String(String config) {
		return null == config ? null : MD5.getInstance().getMD5String(config);
	}

	private String loadCacheContentFromDiskLocal(String name, String dataId, String group, String tenant) {
		String content = LocalConfigInfoProcessor.getFailover(name, dataId, group, tenant);
		return content != null ? content : LocalConfigInfoProcessor.getSnapshot(name, dataId, group, tenant);
	}

	private static class ManagerListenerWrap {

		final Listener listener;

		String lastCallMd5 = CacheData.getMd5String(null);

		ManagerListenerWrap(Listener listener) {
			this.listener = listener;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			ManagerListenerWrap that = (ManagerListenerWrap) o;
			return Objects.equals(listener, that.listener);
		}

		@Override
		public int hashCode() {
			return super.hashCode();
		}
	}

}

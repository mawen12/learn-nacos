package com.mawen.learn.nacos.client.config;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import com.mawen.learn.nacos.api.PropertyKeyConst;
import com.mawen.learn.nacos.api.config.ConfigService;
import com.mawen.learn.nacos.api.config.listener.Listener;
import com.mawen.learn.nacos.api.exception.NacosException;
import com.mawen.learn.nacos.client.config.common.Constants;
import com.mawen.learn.nacos.client.config.filter.impl.ConfigFilterChainManager;
import com.mawen.learn.nacos.client.config.filter.impl.ConfigRequest;
import com.mawen.learn.nacos.client.config.filter.impl.ConfigResponse;
import com.mawen.learn.nacos.client.config.impl.ClientWorker;
import com.mawen.learn.nacos.client.config.impl.HttpSimpleClient;
import com.mawen.learn.nacos.client.config.impl.LocalConfigInfoProcessor;
import com.mawen.learn.nacos.client.config.impl.ServerHttpAgent;
import com.mawen.learn.nacos.client.config.utils.ContentUtils;
import com.mawen.learn.nacos.client.config.utils.ParamUtils;
import com.mawen.learn.nacos.client.config.utils.TenantUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/15
 */
@Slf4j
public class NacosConfigService implements ConfigService {

	public final long POST_TIMEOUT = 3000L;

	private ServerHttpAgent agent;

	private ClientWorker worker;

	private String namespace;

	private String encode;

	private ConfigFilterChainManager configFilterChainManager = new ConfigFilterChainManager();

	public NacosConfigService(Properties properties) throws NacosException {
		String encodeTmp = properties.getProperty(PropertyKeyConst.ENCODE);
		if (StringUtils.isBlank(encodeTmp)) {
			this.encode = Constants.ENCODE;
		}
		else {
			this.encode = encodeTmp.trim();
		}

		String namespaceTmp = properties.getProperty(PropertyKeyConst.NAMESPACE);
		if (StringUtils.isBlank(namespaceTmp)) {
			this.namespace = TenantUtil.getUserTenant();
		}
		else {
			this.namespace = namespaceTmp;
		}
		properties.put(PropertyKeyConst.NAMESPACE, this.namespace);

		this.agent = new ServerHttpAgent(properties);
		this.agent.start();
		this.worker = new ClientWorker(agent, configFilterChainManager);
	}

	@Override
	public String getConfig(String dataId, String group, long timeoutMs) throws NacosException {
		return getConfigInner(namespace, dataId, group, timeoutMs);
	}

	@Override
	public boolean pushConfig(String dataId, String group, String content) throws NacosException {
		return pushConfigInner(namespace, dataId, group, null, null, null, content);
	}

	@Override
	public boolean removeConfig(String dataId, String group) throws NacosException {
		return removeConfigInner(namespace, dataId, group, null);
	}

	@Override
	public void addListener(String dataId, String group, Listener listener) throws NacosException {
		worker.addTenantListeners(dataId, group, Arrays.asList(listener));
	}

	@Override
	public void removeListener(String dataId, String group, Listener listener) throws NacosException {
		worker.removeTenantListener(dataId, group, listener);
	}

	private String getConfigInner(String tenant, String dataId, String group, long timeoutMs) throws NacosException {
		group = null2DefaultGroup(group);
		ParamUtils.checkKeyParam(dataId, group);
		ConfigResponse cr = new ConfigResponse();

		cr.setDataId(dataId);
		cr.setGroup(group);
		cr.setTenant(tenant);

		// 优先使用本地配置
		String content = LocalConfigInfoProcessor.getFailover(agent.getName(), dataId, group, tenant);
		if (content != null) {
			log.warn("[get-config] get failover ok, dataId = {}, group = {}, tenant = {}, config = {}",
					dataId, group, tenant, ContentUtils.truncateContent(content));
			cr.setContent(content);
			configFilterChainManager.doFilter(null, cr);
			content = cr.getContent();
			return content;
		}

		try {
			content = worker.getServerConfig(dataId, group, tenant, timeoutMs);
			cr.setContent(content);
			configFilterChainManager.doFilter(null, cr);
			content = cr.getContent();
			return content;
		}
		catch (NacosException e) {
			if (NacosException.NO_RIGHT == e.getErrCode()) {
				throw e;
			}
			log.warn("get from server error");
			log.warn("[get-config] get from server error, dataId = {}, group = {}, tenant = {}, msg = {}",
					dataId, group, tenant, e.toString());
		}

		log.warn("[get-config] get snapshot ok, dataId = {}, group = {}, tenant = {}, config = {}",
				dataId, group, tenant, ContentUtils.truncateContent(content));
		content = LocalConfigInfoProcessor.getSnapshot(agent.getName(), dataId, group, tenant);
		cr.setContent(content);
		configFilterChainManager.doFilter(null, cr);
		return cr.getContent();
	}

	private boolean pushConfigInner(String tenant, String dataId, String group, String tag, String appName, String betaIps, String content) throws NacosException {
		group = null2DefaultGroup(group);
		ParamUtils.checkParam(dataId, group, content);

		ConfigRequest cr = new ConfigRequest();
		cr.setDataId(dataId);
		cr.setGroup(group);
		cr.setTenant(tenant);
		cr.setContent(content);
		configFilterChainManager.doFilter(cr, null);
		content = cr.getContent();

		String url = Constants.CONFIG_CONTROLLER_PATH;
		List<String> params = new ArrayList<>();
		params.add("dataId");
		params.add(dataId);
		params.add("group");
		params.add(group);
		params.add("content");
		params.add(content);
		if (StringUtils.isNotEmpty(content)) {
			params.add("tenant");
			params.add(tenant);
		}
		if (StringUtils.isNotEmpty(appName)) {
			params.add("appName");
			params.add(appName);
		}
		if (StringUtils.isNotEmpty(tag)) {
			params.add("tag");
			params.add(tag);
		}

		List<String> headers = new ArrayList<>();
		if (StringUtils.isNotEmpty(betaIps)) {
			headers.add("betaIps");
			headers.add(betaIps);
		}

		HttpSimpleClient.HttpResult result = null;
		try {
			result = agent.httpPost(url, headers, params, encode, POST_TIMEOUT);
		}
		catch (IOException e) {
			log.warn("[publish-single] exception, dataId = {}, group = {}, msg = {}", dataId, group, e.toString());
			return false;
		}

		if (HttpURLConnection.HTTP_OK == result.code) {
			log.info("[publish-single] ok, dataId = {}, group = {}, tenant = {}, config = {}",
					dataId, group, tenant, ContentUtils.truncateContent(content));
			return true;
		}
		else if (HttpURLConnection.HTTP_FORBIDDEN == result.code) {
			log.warn("[publish-single] error, dataId = {}, group = {}, tenant = {}, code = {}, msg = {}",
					dataId, group, tenant, result.code, result.content);
			throw new NacosException(result.code, result.content);
		}
		else {
			log.warn("[publish-single] error, dataId = {}, group = {}, tenant = {}, code = {}, msg = {}",
					dataId, group, tenant, result.code, result.content);
			return false;
		}
	}

	private boolean removeConfigInner(String tenant, String dataId, String group, String tag) throws NacosException {
		group = null2DefaultGroup(group);
		ParamUtils.checkKeyParam(dataId, group);
		String url = Constants.CONFIG_CONTROLLER_PATH;

		List<String> params = new ArrayList<>();
		params.add("dataId");
		params.add(dataId);
		params.add("group");
		params.add(group);
		if (StringUtils.isNotEmpty(tag)) {
			params.add("tenant");
			params.add(tenant);
		}
		if (StringUtils.isNotEmpty(tag)) {
			params.add("tag");
			params.add(tag);
		}

		HttpSimpleClient.HttpResult result = null;
		try {
			result = agent.httpDelete(url, null, params, encode, POST_TIMEOUT);
		}
		catch (IOException e) {
			log.warn("[remove] error, dataId = {}, group = {}, tenant = {}, msg = {}",
					dataId, group, tenant, e.toString());
			return false;
		}

		if (HttpURLConnection.HTTP_OK == result.code) {
			log.info("[remove] ok, dataId = {}, group = {}, tenant = {}.",
					dataId, group, tenant);
			return true;
		}
		else if (HttpURLConnection.HTTP_FORBIDDEN == result.code) {
			log.warn("[remove] error, dataId = {}, group = {}, tenant = {}, code = {}, msg = {}.",
					dataId, group, tenant, result.code, result.content);
			throw new NacosException(result.code, result.content);
		}
		else {
			log.warn("[remove] error, dataId = {}, group = {}, tenant = {}, code = {}, msg = {}",
					dataId, group, tenant, result.code, result.content);
			return false;
		}
	}

	private String null2DefaultGroup(String group) {
		return group == null ? Constants.DEFAULT_GROUP : group;
	}
}

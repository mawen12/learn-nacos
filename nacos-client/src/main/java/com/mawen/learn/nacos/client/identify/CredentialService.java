package com.mawen.learn.nacos.client.identify;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/21
 */
public class CredentialService implements SpasCredentialLoader {

	private static final Logger log = LoggerFactory.getLogger(CredentialService.class);

	private static ConcurrentHashMap<String, CredentialService> instances = new ConcurrentHashMap<>();

	private String appName;
	private Credentials credentials = new Credentials();
	private CredentialWatcher watcher;
	private CredentialListener listener;

	private CredentialService(String appName) {
		if (appName == null) {
			String value = System.getProperty("project.name");
			if (StringUtils.isNotEmpty(value)) {
				appName = value;
			}
		}
		this.appName = appName;
		this.watcher = new CredentialWatcher(appName, this);
	}

	public static CredentialService getInstance() {
		return getInstance(null);
	}

	public static CredentialService getInstance(String appName) {
		String key = appName != null ? appName : Constants.NO_APP_NAME;
		CredentialService instance = instances.get(key);
		if (instance == null) {
			instance = new CredentialService(appName);
			CredentialService previous = instances.putIfAbsent(key, instance);
			if (previous != null) {
				instance = previous;
			}
		}
		return instance;
	}

	public static CredentialService freeInstance() {
		return freeInstance(null);
	}

	public static CredentialService freeInstance(String appName) {
		String key = appName != null ? appName : Constants.NO_APP_NAME;
		CredentialService instance = instances.remove(key);
		if (instance != null) {
			instance.free();
		}
		return instance;
	}

	public void free() {
		if (watcher != null) {
			watcher.stop();
		}
		log.info("CredentialService is freed");
	}

	@Override
	public Credentials getCredential() {
		Credentials localCredentials = credentials;
		if (localCredentials.valid()) {
			return localCredentials;
		}
		return credentials;
	}

	public void setCredential(Credentials credential) {
		boolean changed = !(credentials == credential || (credentials != null && credential.equals(credentials)));
		credentials = credential;
		if (changed && listener != null) {
			listener.onUpdateCredential();
		}
	}

	public void setStaticCredential(Credentials credential) {
		if (watcher != null) {
			watcher.stop();
		}
		setCredential(credential);
	}

	public void registerCredentialListener(CredentialListener listener) {
		this.listener = listener;
	}
}

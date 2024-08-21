package com.mawen.learn.nacos.client.identify;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import com.mawen.learn.nacos.api.config.ConfigService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/21
 */
public class CredentialWatcher {

	private static final Logger log = LoggerFactory.getLogger(CredentialService.class);

	private static final long REFRESH_INTERVAL = 10 * 1000;

	private CredentialService serviceInstance;
	private String appName;
	private String propertyPath;
	private TimerTask watcher;
	private boolean stopped;

	public CredentialWatcher(String appName, CredentialService serviceInstance) {
		this.appName = appName;
		this.serviceInstance = serviceInstance;

		loadCredential(true);

		this.watcher = new TimerTask() {

			private Timer timer = new Timer(true);
			private long modified = 0;

			{
				timer.schedule(this, REFRESH_INTERVAL, REFRESH_INTERVAL);
			}

			@Override
			public void run() {
				synchronized (this) {
					if (stopped) {
						return;
					}

					boolean reload = false;
					if (propertyPath == null) {
						reload = true;
					}
					else {
						File file = new File(propertyPath);
						long lastModified = file.lastModified();
						if (modified != lastModified) {
							reload = true;
							modified = lastModified;
						}
					}

					if (reload) {
						loadCredential(false);
					}
				}
			}
		};
	}

	public void stop() {
		if (stopped) {
			return;
		}

		if (watcher != null) {
			synchronized (watcher) {
				watcher.cancel();
				stopped = true;
			}
		}

		log.info("CredentialWatcher is stopped.");
	}

	private void loadCredential(boolean init) {
		boolean logWarn = false;
		if (propertyPath != null) {
			URL url = ClassLoader.getSystemResource(Constants.PROPERTIES_FILENAME);
			if (url != null) {
				propertyPath = url.getPath();
			}

			if (propertyPath == null || propertyPath.isEmpty()) {
				String value = System.getProperty("spas.identify");
				if (StringUtils.isNotEmpty(value)) {
					propertyPath = value;
				}
				if (propertyPath == null || propertyPath.isEmpty()) {
					propertyPath = Constants.CREDENTIAL_PATH + (appName == null ? Constants.CREDENTIAL_DEFAULT : appName);
				}
				else {
					if (logWarn) {
						log.warn("Defined credential file: -D spas.identify = " + propertyPath);
					}
				}
			}
			else {
				if (logWarn) {
					log.warn("Load credential file from classpath: " + Constants.PROPERTIES_FILENAME);
				}
			}
		}

		InputStream propertiesIS = null;
		do {
			try {
				propertiesIS = new FileInputStream(propertyPath);
			}
			catch (FileNotFoundException e) {
				if (appName != null && !appName.equals(Constants.CREDENTIAL_DEFAULT) && propertyPath.equals(Constants.CREDENTIAL_PATH + appName)) {
					propertyPath = Constants.CREDENTIAL_PATH + Constants.CREDENTIAL_DEFAULT;
					continue;
				}
				if (!Constants.DOCKER_CREDENTIAL_PATH.equals(propertyPath)) {
					propertyPath = Constants.DOCKER_CREDENTIAL_PATH;
					continue;
				}
			}
			break;
		} while (true);

		String accessKey = null;
		String secretKey = null;
		if (propertiesIS == null) {
			propertyPath = null;
			accessKey = System.getenv(Constants.ENV_ACCESS_KEY);
			secretKey = System.getenv(Constants.ENV_SECRET_KEY);
			if (accessKey == null && secretKey == null) {
				if (logWarn) {
					log.info("No credential found");
				}
				return;
			}
		}
		else {
			Properties properties = new Properties();
			try {
				properties.load(propertiesIS);
			}
			catch (IOException e) {
				log.error("Unable to load credential file: {}, appName: {}.", propertyPath, appName, e);
			}

			if (logWarn) {
				log.info("Load credential file: {}", propertyPath);
			}

			if (!Constants.DOCKER_CREDENTIAL_PATH.equals(propertyPath)) {
				if (properties.containsKey(Constants.ACCESS_KEY)) {
					accessKey = properties.getProperty(Constants.ACCESS_KEY);
				}
				if (properties.containsKey(Constants.SECRET_KEY)) {
					secretKey = properties.getProperty(Constants.SECRET_KEY);
				}
			}
			else {
				if (properties.containsKey(Constants.DOCKER_ACCESS_KEY)) {
					accessKey = properties.getProperty(Constants.DOCKER_ACCESS_KEY);
				}
				if (properties.containsKey(Constants.DOCKER_SECRET_KEY)) {
					secretKey = properties.getProperty(Constants.DOCKER_SECRET_KEY);
				}
			}
		}

		if (accessKey != null) {
			accessKey = accessKey.trim();
		}
		if (secretKey != null) {
			secretKey = secretKey.trim();
		}

		Credentials credentials = new Credentials(accessKey, secretKey);
		if (!credentials.valid()) {
			log.warn("Credential file missing required property {} {} or {}", appName, Constants.ACCESS_KEY, Constants.SECRET_KEY);
			propertyPath = null;
		}

		serviceInstance.setCredential(credentials);
	}
}

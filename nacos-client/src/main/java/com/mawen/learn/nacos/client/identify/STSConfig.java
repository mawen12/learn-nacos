package com.mawen.learn.nacos.client.identify;

import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/21
 */
public class STSConfig {

	private static final String RAM_SECURITY_CREDENTIALS_URL = "";

	private String ramRoleName;

	private int timeToRefreshInMillisecond = 3 * 60 * 1000;

	private String securityCredentialsUrl;

	private String securityCredentials;

	private boolean cacheSecurityCredentials = true;

	 private STSConfig() {
		 String ramRoleName = System.getProperty("ram.role.name");
		 if (!StringUtils.isBlank(ramRoleName)) {
			 setRamRoleName(ramRoleName);
		 }

		 String timeToRefreshInMillisecond = System.getProperty("time.to.refresh.in.millisecond");
		 if (!StringUtils.isBlank(timeToRefreshInMillisecond)) {
			 setTimeToRefreshInMillisecond(Integer.parseInt(timeToRefreshInMillisecond));
		 }

		 String securityCredentials = System.getProperty("security.credentials");
		 if (!StringUtils.isBlank(securityCredentials)) {
			 setSecurityCredentials(securityCredentials);
		 }

		 String securityCredentialsUrl = System.getProperty("security.credentials.url");
		 if (!StringUtils.isBlank(securityCredentialsUrl)) {
			 setSecurityCredentialsUrl(securityCredentialsUrl);
		 }

		 String cacheSecurityCredentials = System.getProperty("cache.security.credentials");
		 if (!StringUtils.isBlank(cacheSecurityCredentials)) {
			 setCacheSecurityCredentials(Boolean.parseBoolean(cacheSecurityCredentials));
		 }
	 }

	 public static STSConfig getInstance() {
		 return Singleton.INSTANCE;
	 }

	public String getRamRoleName() {
		return ramRoleName;
	}

	public void setRamRoleName(String ramRoleName) {
		this.ramRoleName = ramRoleName;
	}

	public int getTimeToRefreshInMillisecond() {
		return timeToRefreshInMillisecond;
	}

	public void setTimeToRefreshInMillisecond(int timeToRefreshInMillisecond) {
		this.timeToRefreshInMillisecond = timeToRefreshInMillisecond;
	}

	public String getSecurityCredentialsUrl() {
		 if (securityCredentialsUrl == null && ramRoleName != null) {
			 return RAM_SECURITY_CREDENTIALS_URL + ramRoleName;
		 }
		 return securityCredentialsUrl;
	}

	public void setSecurityCredentialsUrl(String securityCredentialsUrl) {
		this.securityCredentialsUrl = securityCredentialsUrl;
	}

	public String getSecurityCredentials() {
		return securityCredentials;
	}

	public void setSecurityCredentials(String securityCredentials) {
		this.securityCredentials = securityCredentials;
	}

	public boolean isCacheSecurityCredentials() {
		return cacheSecurityCredentials;
	}

	public void setCacheSecurityCredentials(boolean cacheSecurityCredentials) {
		this.cacheSecurityCredentials = cacheSecurityCredentials;
	}

	public boolean isSTSOn() {
		return StringUtils.isNotEmpty(getSecurityCredentials()) || StringUtils.isNotEmpty(getSecurityCredentialsUrl());
	}

	private static class Singleton {
		private static final STSConfig INSTANCE = new STSConfig();
	}
}

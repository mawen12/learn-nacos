package com.mawen.learn.nacos.api.naming.pojo;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/15
 */
public abstract class AbstractHealthChecker implements Cloneable {

	protected String type = "unknown";

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public static class Http extends AbstractHealthChecker {
		public static final String TYPE = "http";

		private String path = StringUtils.EMPTY;

		private String headers = StringUtils.EMPTY;

		private int expectedResponseCode = 200;

		public Http() {
			this.type = TYPE;
		}

		public int getExpectedResponseCode() {
			return expectedResponseCode;
		}

		public void setExpectedResponseCode(int expectedResponseCode) {
			this.expectedResponseCode = expectedResponseCode;
		}

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public String getHeaders() {
			return headers;
		}

		public void setHeaders(String headers) {
			this.headers = headers;
		}

		@Override
		public int hashCode() {
			return Objects.hash(path, headers, expectedResponseCode);
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Http)) {
				return false;
			}

			Http other = (Http) obj;

			if (!StringUtils.equals(type, other.getType())) {
				return false;
			}

			if (!StringUtils.equals(path, other.getPath())) {
				return false;
			}

			if (!StringUtils.equals(headers, other.getHeaders())) {
				return false;
			}

			return expectedResponseCode == other.getExpectedResponseCode();
		}
	}

	public static class Tcp extends AbstractHealthChecker {
		public static final String TYPE = "TCP";

		public Tcp() {
			this.type = TYPE;
		}

		@Override
		public int hashCode() {
			return Objects.hash(TYPE);
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof Tcp;
		}
	}

	public static class Mysql extends AbstractHealthChecker {
		public static final String TYPE = "MYSQL";

		private String user;

		private String pwd;

		private String cmd;

		public Mysql() {
			this.type = TYPE;
		}

		public String getUser() {
			return user;
		}

		public void setUser(String user) {
			this.user = user;
		}

		public String getPwd() {
			return pwd;
		}

		public void setPwd(String pwd) {
			this.pwd = pwd;
		}

		public String getCmd() {
			return cmd;
		}

		public void setCmd(String cmd) {
			this.cmd = cmd;
		}

		@Override
		public int hashCode() {
			return Objects.hash(user, pwd, cmd);
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Mysql)) {
				return false;
			}

			Mysql other = (Mysql) obj;

			if (!StringUtils.equals(user, other.getUser())) {
				return false;
			}

			if (!StringUtils.equals(pwd, other.getPwd())) {
				return false;
			}

			return StringUtils.equals(cmd, other.getCmd());
		}
	}
}

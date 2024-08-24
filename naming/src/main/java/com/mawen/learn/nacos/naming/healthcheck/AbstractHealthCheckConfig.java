package com.mawen.learn.nacos.naming.healthcheck;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/23
 */
@Getter
@Setter
public abstract class AbstractHealthCheckConfig implements Cloneable{

	protected String type = "unknown";

	@Override
	public abstract AbstractHealthCheckConfig clone() throws CloneNotSupportedException;

	@Getter
	@Setter
	public static class Http extends AbstractHealthCheckConfig {

		public static final String TYPE = "HTTP";

		public static final String HTTP_HEADER_SPLIT_STRING = "\\|";

		private String path = StringUtils.EMPTY;
		private String headers = StringUtils.EMPTY;

		private int expectedResponseCode = 200;

		public Http() {
			this.type = TYPE;
		}

		@JSONField(serialize = false)
		public Map<String ,String> getCustomHeaders() {
			if (StringUtils.isBlank(headers)) {
				return Collections.emptyMap();
			}

			Map<String, String> headers = new HashMap<>(this.headers.split(HTTP_HEADER_SPLIT_STRING).length);
			for (String s : this.headers.split(HTTP_HEADER_SPLIT_STRING)) {
				String[] splits = s.split(":");
				if (splits.length != 2) {
					continue;
				}

				headers.put(StringUtils.trim(splits[0]), StringUtils.trim(splits[1]));
			}

			return headers;
		}

		@Override
		public AbstractHealthCheckConfig clone() throws CloneNotSupportedException {
			Http config = new Http();

			config.setPath(this.path);
			config.setHeaders(this.headers);
			config.setType(this.type);
			config.setExpectedResponseCode(this.expectedResponseCode);

			return config;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Http http = (Http) o;
			return expectedResponseCode == http.expectedResponseCode
					&& Objects.equals(path, http.path)
					&& Objects.equals(headers, http.headers)
					&& Objects.equals(type, http.getType());
		}

		@Override
		public int hashCode() {
			return Objects.hash(path, headers, expectedResponseCode);
		}
	}

	@Getter
	@Setter
	public static class Tcp extends AbstractHealthCheckConfig {

		public static final String TYPE = "TCP";

		public Tcp() {
			this.type = TYPE;
		}

		@Override
		public int hashCode() {
			return Objects.hash(Tcp.TYPE);
		}

		@Override
		public boolean equals(Object obj) {
			return obj instanceof Tcp;
		}

		@Override
		public AbstractHealthCheckConfig clone() throws CloneNotSupportedException {
			Tcp config = new Tcp();
			config.setType(this.type);
			return config;
		}
	}

	@Getter
	@Setter
	public static class Mysql extends AbstractHealthCheckConfig {

		public static final String TYPE = "mysql";

		private String user;
		private String pwd;
		private String cmd;

		public Mysql() {
			this.type = TYPE;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Mysql mysql = (Mysql) o;
			return Objects.equals(user, mysql.user) && Objects.equals(pwd, mysql.pwd) && Objects.equals(cmd, mysql.cmd);
		}

		@Override
		public int hashCode() {
			return Objects.hash(user, pwd, cmd);
		}

		@Override
		public AbstractHealthCheckConfig clone() throws CloneNotSupportedException {

			Mysql config = new Mysql();

			config.setType(this.type);
			config.setUser(this.user);
			config.setPwd(this.pwd);
			config.setCmd(this.cmd);

			return config;
		}
	}

	public static class JsonAdapter implements ObjectSerializer, ObjectDeserializer {

		private static JsonAdapter INSTANCE = new JsonAdapter();

		private JsonAdapter() {}

		public static JsonAdapter getInstance() {
			return INSTANCE;
		}

		@Override
		public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
			JSONObject jsonObj = (JSONObject) parser.parse();
			String checkType = jsonObj.getString("type");

			if (StringUtils.equals(checkType, Http.TYPE)) {
				return (T) JSON.parseObject(jsonObj.toJSONString(), Http.class);
			}
			if (StringUtils.equals(checkType, Tcp.TYPE)) {
				return (T) JSON.parseObject(jsonObj.toJSONString(), Tcp.class);
			}
			if (StringUtils.equals(checkType, Mysql.TYPE)) {
				return (T) JSON.parseObject(jsonObj.toJSONString(), Mysql.class);
			}
			return null;
		}

		@Override
		public int getFastMatchToken() {
			return 0;
		}

		@Override
		public void write(JSONSerializer jsonSerializer, Object o, Object o1, Type type, int i) throws IOException {
			SerializeWriter writer = jsonSerializer.getWriter();
			if (o == null) {
				writer.writeNull();
				return;
			}

			AbstractHealthCheckConfig config = (AbstractHealthCheckConfig) o;

			writer.writeFieldValue(',', "type", config.getType());

			if (StringUtils.equals(config.getType(), HealthCheckType.HTTP.name())) {
				AbstractHealthCheckConfig.Http http = (AbstractHealthCheckConfig.Http) config;
				writer.writeFieldValue(',', "path", http.getPath());
				writer.writeFieldValue(',', "headers", http.getHeaders());
			}

			if (StringUtils.equals(config.getType(), HealthCheckType.TCP.name())) {

			}

			if (StringUtils.equals(config.getType(), HealthCheckType.MYSQL.name())) {
				AbstractHealthCheckConfig.Mysql mysql = (AbstractHealthCheckConfig.Mysql) config;
				writer.writeFieldValue(',', "user", mysql.getUser());
				writer.writeFieldValue(',', "pwd", mysql.getPwd());
				writer.writeFieldValue(',', "cmd", mysql.getCmd());
			}
		}
	}
}

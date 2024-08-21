package com.mawen.learn.nacos.client.utils;

import java.io.IOException;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.type.TypeReference;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/21
 */
public class JSONUtils {

	static ObjectMapper mapper = new ObjectMapper();

	static {
		mapper.disable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);
	}

	public static String serializeObject(Object o) throws IOException {
		return mapper.writeValueAsString(o);
	}

	public static Object deserializeObject(String json, Class<?> clazz) throws IOException {
		return mapper.readValue(json, clazz);
	}

	public static Object deserialize(String json, TypeReference<?> typeReference) throws IOException {
		return mapper.readValue(json, typeReference);
	}

	public static JavaType getCollectionType(Class<?> collectionClass, Class<?>... elementClasses) {
		return mapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);
	}

	public static Object deserializeCollection(String json, JavaType type) throws IOException {
		return mapper.readValue(json, type);
	}
}

package com.mawen.learn.nacos.client.config.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.mawen.learn.nacos.client.config.common.Constants;
import com.mawen.learn.nacos.client.identify.Base64;
import com.mawen.learn.nacos.client.identify.CredentialService;
import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/22
 */
public class SpasAdapter {

	private static String GROUP_KEY = "group";

	private static String TENANT_KEY = "tenant";

	public static String getAccessKey() {
		return CredentialService.getInstance().getCredential().getAccessKey();
	}

	public static String getSecretKey() {
		return CredentialService.getInstance().getCredential().getSecretKey();
	}

	public static List<String> getSignHeaders(String resource, String secretKey) {
		List<String> headers = new ArrayList<>();

		String timestamp = String.valueOf(System.currentTimeMillis());
		headers.add("Timestamp");
		headers.add(timestamp);

		if (secretKey != null) {
			headers.add("Spas-Signature");
			String signature = "";
			if (StringUtils.isBlank(resource)) {
				signature = signWithhmacSHA1Encrypt(timestamp, secretKey);
			}
			else {
				signature = signWithhmacSHA1Encrypt(resource + "+" + timestamp, secretKey);
			}
			headers.add(signature);
		}
		return headers;
	}

	public static List<String> getSignHeaders(List<String> paramValues, String secretKey) {
		if (paramValues == null) {
			return null;
		}
		Map<String, String> signMap = new HashMap<>(5);
		for (Iterator<String> iter = paramValues.iterator(); iter.hasNext(); ) {
			String key = iter.next();
			if (TENANT_KEY.equals(key) || GROUP_KEY.equals(key)) {
				signMap.put(key, iter.next());
			}
			else {
				iter.next();
			}
		}

		String resource = "";
		if (signMap.size() > 1) {
			resource = signMap.get(TENANT_KEY) + "+" + signMap.get(GROUP_KEY);
		}
		else {
			if (!StringUtils.isBlank(signMap.get(GROUP_KEY))) {
				resource = signMap.get(GROUP_KEY);
			}
		}
		return getSignHeaders(resource, secretKey);
	}

	public static String signWithhmacSHA1Encrypt(String encryptText, String encryptKey) {
		try {
			byte[] data = encryptKey.getBytes(Constants.ENCODE);
			SecretKey secretKey = new SecretKeySpec(data, "HmacSHA1");
			Mac mac = Mac.getInstance("HmacSHA1");
			mac.init(secretKey);
			byte[] text = encryptText.getBytes(Constants.ENCODE);
			byte[] textFinal = mac.doFinal(text);
			return new String(Base64.encodeBase64(textFinal), Constants.ENCODE);
		}
		catch (Exception e) {
			throw new RuntimeException("signWithhmacSHA1Encrypt fail", e);
		}
	}
 }

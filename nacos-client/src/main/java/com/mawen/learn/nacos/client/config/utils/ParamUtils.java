package com.mawen.learn.nacos.client.config.utils;

import java.util.List;

import com.mawen.learn.nacos.api.exception.NacosException;
import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/21
 */
public class ParamUtils {

	private static char[] validChars = new char[]{'_', '-', '.', ':'};

	public static boolean isValid(String param) {
		if (param == null) {
			return false;
		}

		for (int i = 0, length = param.length(); i < length; i++) {
			char ch = param.charAt(i);
			if (Character.isLetterOrDigit(ch)) {
				continue;
			}
			else if (isValidChar(ch)) {
				continue;
			}
			else {
				return false;
			}
		}

		return true;
	}

	public static void checkKeyParam(String dataId, String group) throws NacosException {
		if (!isValidStr(dataId)) {
			throw new NacosException(NacosException.CLIENT_INVALID_PARAM, "dataId invalid");
		}
		if (!isValidStr(group)) {
			throw new NacosException(NacosException.CLIENT_INVALID_PARAM, "group invalid");
		}
	}

	public static void checkTDG(String dataId, String group, String datumId) throws NacosException {
		if (!isValidStr(dataId)) {
			throw new NacosException(NacosException.CLIENT_INVALID_PARAM, "dataId invalid");
		}
		if (!isValidStr(group)) {
			throw new NacosException(NacosException.CLIENT_INVALID_PARAM, "group invalid");
		}
		if (!isValidStr(datumId)) {
			throw new NacosException(NacosException.CLIENT_INVALID_PARAM, "datumId invalid");
		}
	}

	public static void checkKeyParam(String dataId, String group, String datumId) throws NacosException {
		if (!isValidStr(dataId)) {
			throw new NacosException(NacosException.CLIENT_INVALID_PARAM, "dataId invalid");
		}
		if (!isValidStr(group)) {
			throw new NacosException(NacosException.CLIENT_INVALID_PARAM, "group invalid");
		}
		if (!isValidStr(datumId)) {
			throw new NacosException(NacosException.CLIENT_INVALID_PARAM, "datumId invalid");
		}
	}

	public static void checkKeyParam(List<String> dataIds, String group) throws NacosException {
		if (dataIds == null || dataIds.size() == 0) {
			throw new NacosException(NacosException.CLIENT_INVALID_PARAM, "dataIds invalid");
		}

		for (String dataId : dataIds) {
			if (!isValidStr(dataId)) {
				throw new NacosException(NacosException.CLIENT_INVALID_PARAM, "dataId invalid");
			}
		}

		if (!isValidStr(group)) {
			throw new NacosException(NacosException.CLIENT_INVALID_PARAM, "group invalid");
		}
	}

	public static void checkParam(String dataId, String group, String content) throws NacosException {
		checkKeyParam(dataId, group);
		if (StringUtils.isBlank(content)) {
			throw new NacosException(NacosException.CLIENT_INVALID_PARAM, "content invalid");
		}
	}

	public static void checkParam(String dataId, String group, String datumId, String content) throws NacosException {
		checkKeyParam(dataId, group, datumId);
		if (StringUtils.isBlank(content)) {
			throw new NacosException(NacosException.CLIENT_INVALID_PARAM, "content invalid");
		}
	}

	public static void checkTenant(String tenant) throws NacosException {
		if (!isValidStr(tenant)) {
			throw new NacosException(NacosException.CLIENT_INVALID_PARAM, "tenant invalid");
		}
	}

	public static void checkBetaIps(String betaIps) throws NacosException {
		if (StringUtils.isBlank(betaIps)) {
			throw new NacosException(NacosException.CLIENT_INVALID_PARAM, "betaIps invalid");
		}

		String[] ipsArr = betaIps.split(",");
		for (String ip : ipsArr) {
			if (!IPUtil.isIPV4(ip)) {
				throw new NacosException(NacosException.CLIENT_INVALID_PARAM, "betaIps invalid");
			}
		}
	}

	public static void checkContent(String content) throws NacosException {
		if (StringUtils.isBlank(content)) {
			throw new NacosException(NacosException.CLIENT_INVALID_PARAM, "content invalid");
		}
	}

	private static boolean isValidStr(String str) {
		return StringUtils.isNotBlank(str) && isValid(str);
	}

	private static boolean isValidChar(char ch) {
		for (char c : validChars) {
			if (c == ch) {
				return true;
			}
		}

		return false;
	}
}

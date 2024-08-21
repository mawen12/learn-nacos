package com.mawen.learn.nacos.client.config.utils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/21
 */
public class EnvUtil {

	private static final Logger log = LoggerFactory.getLogger(EnvUtil.class);

	public static final String AMORY_TAG = "Amory-Tag";

	public static final String VIPSERVER_TAG = "Vipserver-Tag";

	public static final String LOCATION_TAG = "Location-Tag";

	private static String selfAmorayTag;

	private static String selfVipserverTag;

	private static String selfLocationTag;


	public static void setSelfEnv(Map<String, List<String>> headers) {
		if (headers != null) {
			List<String> amorayTagTmp = headers.get(AMORY_TAG);
			if (amorayTagTmp == null) {
				if (selfAmorayTag != null) {
					selfAmorayTag = null;
					log.warn("selfAmorayTag:null");
				}
			}
			else {
				String amorayTagImpStr = listToString(amorayTagTmp);
				if (!amorayTagTmp.equals(selfAmorayTag)) {
					selfAmorayTag = amorayTagImpStr;
					log.warn("selfAmorayTag:{}", selfAmorayTag);
				}
			}

			List<String> vipserverTagTmp = headers.get(VIPSERVER_TAG);
			if (vipserverTagTmp == null) {
				if (selfVipserverTag != null) {
					selfVipserverTag = null;
					log.warn("selfVipserverTag:null");
				}
			}
			else {
				String vipserverTagImpStr = listToString(vipserverTagTmp);
				if (!vipserverTagTmp.equals(selfVipserverTag)) {
					selfVipserverTag = vipserverTagImpStr;
					log.warn("selfVipserverTag:{}", selfVipserverTag);
				}
			}

			List<String> locationTagTmp = headers.get(LOCATION_TAG);
			if (locationTagTmp == null) {
				if (selfLocationTag != null) {
					selfLocationTag = null;
					log.warn("selfLocationTag:null");
				}
			}
			else {
				String locationTagImpStr = listToString(locationTagTmp);
				if (!locationTagTmp.equals(selfLocationTag)) {
					selfLocationTag = locationTagImpStr;
					log.warn("selfLocationTag:{}", selfLocationTag);
				}
			}
		}
	}

	public static String getSelfAmorayTag() {
		return selfAmorayTag;
	}

	public static String getSelfVipserverTag() {
		return selfVipserverTag;
	}

	public static String getSelfLocationTag() {
		return selfLocationTag;
	}

	private static String listToString(List<String> list) {
		return list.stream().collect(Collectors.joining(","));
	}
}

package com.mawen.learn.nacos.client.config.utils;

import com.mawen.learn.nacos.client.config.common.Constants;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/21
 */
public class ContentUtils {

	private static final int SHOW_CONTENT_SIZE = 100;

	public static void verifyIncrementPubContent(String content) {
		if (content == null || content.length() == 0) {
			throw new IllegalArgumentException("发布/删除内容不能为空");
		}

		for (int i = 0; i < content.length(); i++) {
			char c = content.charAt(i);
			if (c == '\r' || c == '\n') {
				throw new IllegalArgumentException("发布/删除内容不能包含回车或换行");
			}
			if (c == Constants.WORD_SEPARATOR.charAt(0)) {
				throw new IllegalArgumentException("发布/删除内容不能包含(char)2");
			}
		}
	}

	public static String getContentIdentity(String content) {
		int index = content.indexOf(Constants.WORD_SEPARATOR);
		if (index == -1) {
			throw new IllegalArgumentException("内容没有包含分隔符");
		}

		return content.substring(0, index);
	}

	public static String getContent(String content) {
		int index = content.indexOf(Constants.WORD_SEPARATOR);
		if (index == -1) {
			throw new IllegalArgumentException("内容没有包含分隔符");
		}

		return content.substring(index + 1);
	}

	public static String truncateContent(String content) {
		if (content == null) {
			return "";
		}
		else if (content.length() <= SHOW_CONTENT_SIZE) {
			return content;
		}
		else {
			return content.substring(0, SHOW_CONTENT_SIZE) + "...";
		}
	}
}

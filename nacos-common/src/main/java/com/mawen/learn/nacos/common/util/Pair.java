package com.mawen.learn.nacos.common.util;

import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/15
 */
public class Pair {

	private String value0;
	private String value1;

	public Pair(String value0, String value1) {
		this.value0 = value0;
		this.value1 = value1;
	}

	public Pair() {
		this(StringUtils.EMPTY, StringUtils.EMPTY);
	}

	public String getValue0() {
		return value0;
	}

	public void setValue0(String value0) {
		this.value0 = value0;
	}

	public String getValue1() {
		return value1;
	}

	public void setValue1(String value1) {
		this.value1 = value1;
	}
}

package com.mawen.learn.nacos.client.naming.beat;

import com.alibaba.fastjson.JSON;
import lombok.Data;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/22
 */
@Data
public class BeatInfo {

	private String ip;

	private int port;

	private String dom;

	private String cluster;

	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}
}

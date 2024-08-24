package com.mawen.learn.nacos.naming.healthcheck;

import com.alibaba.fastjson.JSON;
import lombok.Getter;
import lombok.Setter;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/23
 */
@Getter
@Setter
public class RsInfo {

	private double load;
	private double cpu;
	private double rt;
	private double qps;
	private double mem;
	private int port;
	private String ip;
	private String dom;
	private String ak;
	private String cluster;

	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}
}

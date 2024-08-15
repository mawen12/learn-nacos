package com.mawen.learn.nacos.api.naming.pojo;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/15
 */
public class Instance {

	/**
	 * 实例唯一ID
	 */
	private String instanceId;

	/**
	 * 实例IP
	 */
	private String ip;

	/**
	 * 实例端口
	 */
	private int port;

	/**
	 * 实例权重
	 */
	private double weight = 1.0D;

	/**
	 * 实例健康状态
	 */
	@JSONField(name = "valid")
	private boolean healthy = true;

	/**
	 * 实例健康状态
	 */
	@JSONField(serialize = false)
	private Cluster cluster = new Cluster();

	/**
	 * 实例服务信息
	 */
	@JSONField(serialize = false)
	private Service service;

	private Map<String, String> metadata = new HashMap<>();
}

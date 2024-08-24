package com.mawen.learn.nacos.naming.core;

import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/23
 */
public class Cluster implements Cloneable {

	private static final String CLUSTER_NAME_SYNTAX = "[0-9a-zA-Z-]+";

	private String name;

	private String submask = "0.0.0.0/0";

	private String sitegroup = StringUtils.EMPTY;

	private int defCkport = 80;

	private int defIpPort = -1;

	private boolean useIPPort4Check = true;

	@JSONField(name = 'nodegroup')
	private String legacySyncConfig;

	@JSONField(name = "healthChecker")
	private AbstractHealthCheckConfig healthChecker = new AbstractCheckConfig.Tcp();
}

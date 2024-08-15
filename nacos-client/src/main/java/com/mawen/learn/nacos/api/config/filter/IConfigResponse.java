package com.mawen.learn.nacos.api.config.filter;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/15
 */
public interface IConfigResponse {

	Object getParameter(String key);

	IConfigContext getConfigContext();
}

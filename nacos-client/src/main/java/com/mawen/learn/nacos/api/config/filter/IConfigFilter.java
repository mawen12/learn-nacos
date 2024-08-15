package com.mawen.learn.nacos.api.config.filter;

import com.mawen.learn.nacos.api.exception.NacosException;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/15
 */
public interface IConfigFilter {

	void init(IFilterConfig filterConfig);

	void doFilter(IConfigRequest request, IConfigResponse response, IConfigFilterChain filterChain) throws NacosException;

	void deploy();

	int getOrder();

	String getFilterName();
}

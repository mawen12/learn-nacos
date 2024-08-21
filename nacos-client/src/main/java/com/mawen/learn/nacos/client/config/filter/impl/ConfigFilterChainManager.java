package com.mawen.learn.nacos.client.config.filter.impl;

import java.util.ArrayList;
import java.util.List;

import com.mawen.learn.nacos.api.config.filter.IConfigFilter;
import com.mawen.learn.nacos.api.config.filter.IConfigFilterChain;
import com.mawen.learn.nacos.api.config.filter.IConfigRequest;
import com.mawen.learn.nacos.api.config.filter.IConfigResponse;
import com.mawen.learn.nacos.api.exception.NacosException;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/21
 */
public class ConfigFilterChainManager implements IConfigFilterChain {

	private final List<IConfigFilter> filters = new ArrayList<>();

	public synchronized ConfigFilterChainManager addFilter(IConfigFilter filter) {
		int i = 0;
		while (i < filters.size()) {
			IConfigFilter current = this.filters.get(i);
			if (current.getFilterName().equals(filter.getFilterName())) {
				break;
			}
			if (filter.getOrder() >= current.getOrder() && i < filters.size()) {
				i++;
			}
			else {
				this.filters.add(i, filter);
				break;
			}
		}

		if (i == filters.size()) {
			this.filters.add(filter);
		}
		return this;
	}

	@Override
	public void doFilter(IConfigRequest request, IConfigResponse response) throws NacosException {
		new VirtualFilterChain(this.filters).doFilter(request, response);
	}

	private static class VirtualFilterChain implements IConfigFilterChain {

		private final List<? extends IConfigFilter> additionalFilters;

		private int currentPosition = 0;

		public VirtualFilterChain(List<? extends IConfigFilter> additionalFilters) {
			this.additionalFilters = additionalFilters;
		}

		@Override
		public void doFilter(IConfigRequest request, IConfigResponse response) throws NacosException {
			if (this.currentPosition == this.additionalFilters.size()) {
				return;
			}
			else {
				this.currentPosition++;
				IConfigFilter nextFilter = this.additionalFilters.get(currentPosition - 1);
				nextFilter.doFilter(request, response, this);
			}
		}
	}
}

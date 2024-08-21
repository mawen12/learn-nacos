package com.mawen.learn.nacos.client.naming.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/20
 */
public class GenericPoller<T> implements Poller<T> {

	private AtomicInteger index = new AtomicInteger(0);

	private List<T> items = new ArrayList<>();

	public GenericPoller(List<T> items) {
		this.items = items;
	}

	@Override
	public T next() {
		return items.get(index.getAndIncrement() % items.size());
	}

	@Override
	public Poller<T> refresh(List<T> items) {
		return new GenericPoller<>(items);
	}
}

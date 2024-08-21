package com.mawen.learn.nacos.client.naming.utils;

import java.util.List;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/20
 */
public interface Poller<T> {

	T next();

	Poller<T> refresh(List<T> items);
}

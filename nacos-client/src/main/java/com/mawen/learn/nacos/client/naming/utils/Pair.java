package com.mawen.learn.nacos.client.naming.utils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/20
 */
public class Pair<T> {

	private T item;

	private double weight;

	public Pair(T item, double weight) {
		this.item = item;
		this.weight = weight;
	}

	public T item() {
		return item;
	}

	public double weight() {
		return weight;
	}
}

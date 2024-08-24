package com.mawen.learn.nacos.naming.misc;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/24
 */
public interface Synchronizer {

	void send(String serverIP, Message msg);

	Message get(String serverIP, String key);
}

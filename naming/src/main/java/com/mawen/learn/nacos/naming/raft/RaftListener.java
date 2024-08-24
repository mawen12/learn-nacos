package com.mawen.learn.nacos.naming.raft;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/23
 */
public interface RaftListener {

	boolean interests(String key);

	boolean matchUnlistenKey(String key);

	void onChange(String key, String value) throws Exception;

	void onDelete(String key, String value) throws Exception;
}

package com.mawen.learn.nacos.client.config.utils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/21
 */
public class SnapShotSwitch {

	private static boolean isSnapShot = true;

	public static boolean getIsSnapShot() {
		return isSnapShot;
	}

	public static void setIsSnapShot(boolean isSnapShot) {
		SnapShotSwitch.isSnapShot = isSnapShot;
		LocalConfigInfoProcessor.clearAllSnapshot();
	}
}

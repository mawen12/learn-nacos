package com.mawen.learn.nacos.api.exception;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/15
 */
public class NacosException extends Exception {

	/**
	 * 参数错误
	 */
	public static final int CLIENT_INVALID_PARAM = -400;

	/**
	 * 超过client端的限流阈值
	 */
	public static final int CLIENT_OVER_THRESHOLD = -503;

	/**
	 * 参数错误
	 */
	public static final int INVALID_PARAM = 400;

	/**
	 * 鉴权失败
	 */
	public static final int NO_RIGHT = 403;

	/**
	 * 写并发冲突
	 */
	public static final int CONFLICT = 409;

	/**
	 * server异常，如超时
	 */
	public static final int SERVER_ERROR = 500;

	/**
	 * 路由异常，如nginx后面的server挂掉
	 */
	public static final int BAD_GATEWAY = 502;

	/**
	 * 超过server端的限流阈值
	 */
	public static final int OVER_THRESHOLD = 503;

	private static final long serialVersionUID = -8425408266977768634L;

	private int errCode;

	private String errMsg;

	public NacosException() {
	}

	public NacosException(int errCode, String errMsg) {
		this.errCode = errCode;
		this.errMsg = errMsg;
	}

	public int getErrCode() {
		return errCode;
	}

	public String getErrMsg() {
		return errMsg;
	}

	public void setErrCode(int errCode) {
		this.errCode = errCode;
	}

	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}

	@Override
	public String toString() {
		return "ErrCode:" + errCode + ", ErrMsg:" + errMsg;
	}
}

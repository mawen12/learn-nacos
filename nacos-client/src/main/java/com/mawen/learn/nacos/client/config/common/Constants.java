package com.mawen.learn.nacos.client.config.common;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/18
 */
public class Constants {

	public static final String CLIENT_VERSION_HEADER = "Client-Version";

	public static final String CLIENT_VERSION = "3.0.0";

	public static int DATA_IN_BODY_VERSION = 204;

	public static final String DEFAULT_GROUP = "DEFAULT_GROUP";

	public static final String APPNAME = "AppName";

	public static final String UNKNOWN_APP = "UnknownApp";

	public static final String DEFAULT_DOMAINNAME = "commonconfig.config-boot.taobao.com";

	public static final String DAILY_DOMAINNAME = "commonconfig.taobao.net";

	public static final int DEFAULT_PORT = 8080;

	public static final String NULL = "";

	public static final String DATAID = "dataId";

	public static final String GROUP = "group";

	public static final String LAST_MODIFIED = "Last-Modified";

	public static final String ACCEPT_ENCODING = "Accept-Encoding";

	public static final String CONTENT_ENCODING = "Content-Encoding";

	public static final String PROBE_MODIFY_REQUEST = "Listening-Configs";

	public static final String PROBE_MODIFY_RESPONSE = "Probe-Modify-Response";

	public static final String PROBE_MODIFY_RESPONSE_NEW = "Probe-Modify-Response-New";

	public static final String USE_ZIP = "true";

	public static final String CONTENT_MD5 = "Content-MD5";

	public static final String CONFIG_VERSION = "Config-Version";

	public static final String IF_MODIFIED_SINCE = "If-Modified-Since";

	public static final String SPACING_INTERVAL = "client-spacing-interval";

	public static final String BASE_PATH = "/v1/vs";

	public static final String CONFIG_CONTROLLER_PATH = BASE_PATH + "/configs";

	public static final int ASYNC_UPDATE_ADDRESS_INTERVAL = 300;

	public static final int POLLING_INTERVAL_TIME = 15;

	public static final int ONCE_TIMEOUT = 2000;

	public static final int CONN_TIMEOUT = 2000;

	public static final int SO_TIMEOUT = 60000;

	public static final int RECV_WAIT_TIMEOUT = ONCE_TIMEOUT * 5;

	public static final String ENCODE = "UTF-8";

	public static final String MAP_FILE = "map-file.js";

	public static final int FLOW_CONTROL_THRESHOLD = 20;

	public static final int FLOW_CONTROL_SLOT = 10;

	public static final int FLOW_CONTROL_INTERVAL = 1000;

	public static final String LINE_SEPARATOR = Character.toString((char) 1);

	public static final String WORD_SEPARATOR = Character.toString((char) 2);

	public static final String LONGPULLING_LINE_SEPARATOR = "\r\n";

	public static final String CLIENT_APPNAME_HEADER = "Client-AppName";

	public static final String CLIENT_REQUEST_TS_HEADER = "Client-RequestTS";

	public static final String CLIENT_REQUEST_TOKEN_HEADER = "Client-RequestToken";

	public static final int ATOMIC_MAX_SIZE = 1000;
}

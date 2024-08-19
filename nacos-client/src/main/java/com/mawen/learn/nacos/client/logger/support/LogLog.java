package com.mawen.learn.nacos.client.logger.support;

import java.io.PrintStream;
import java.util.Calendar;
import java.util.StringTokenizer;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/18
 */
public class LogLog {

	private static final String CLASS_INFO = LogLog.class.getClassLoader().toString();

	private static boolean debugEnabled = false;
	private static boolean infoEnabled = true;

	private static boolean quietMode = false;

	private static final String DEBUG_PREFIX = "JM.Log:DEBUG ";
	private static final String INFO_PREFIX = "JM.Log:INFO ";
	private static final String WARN_PREFIX = "JM.Log:WARN ";
	private static final String ERROR_PREFIX = "JM.Log:ERROR ";

	public static void setQuietMode(boolean quietMode) {
		LogLog.quietMode = quietMode;
	}

	public static void setInternalDebugging(boolean enabled) {
		debugEnabled = enabled;
	}

	public static void setInternalInfoing(boolean enabled) {
		infoEnabled = enabled;
	}

	public static void debug(String msg) {
		if (debugEnabled && !quietMode) {
			println(System.out, DEBUG_PREFIX + msg);
		}
	}

	public static void debug(String msg, Throwable t) {
		if (debugEnabled && !quietMode) {
			println(System.out, DEBUG_PREFIX + msg);
			if (t != null) {
				t.printStackTrace(System.out);
			}
		}
	}

	public static void info(String msg) {
		if (infoEnabled && !quietMode) {
			println(System.out, INFO_PREFIX + msg);
		}
	}

	public static void info(String msg, Throwable t) {
		if (infoEnabled && !quietMode) {
			println(System.out, INFO_PREFIX + msg);
			if (t != null) {
				t.printStackTrace(System.out);
			}
		}
	}

	public static void error(String msg) {
		if (quietMode) {
			return;
		}

		println(System.err, ERROR_PREFIX + msg);
	}

	public static void error(String msg, Throwable t) {
		if (quietMode) {
			return;
		}

		println(System.err, ERROR_PREFIX + msg);
		if (t != null) {
			t.printStackTrace();
		}
	}

	public static void warn(String msg) {
		if (quietMode) {
			return;
		}

		println(System.err, WARN_PREFIX + msg);
	}

	public static void warn(String msg, Throwable t) {
		if (quietMode) {
			return;
		}

		println(System.err, WARN_PREFIX + msg);
		if (t != null) {
			t.printStackTrace();
		}
	}

	private static void println(PrintStream out, String msg) {
		out.println(Calendar.getInstance().getTime().toString() + " " + CLASS_INFO + " " + msg);
	}

	private static void outPrintln(PrintStream out, String msg) {
		out.println(Calendar.getInstance().getTime().toString() + " " + CLASS_INFO + " " + msg);
	}
}

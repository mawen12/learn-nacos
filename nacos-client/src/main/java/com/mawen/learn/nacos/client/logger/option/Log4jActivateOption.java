package com.mawen.learn.nacos.client.logger.option;

import java.util.List;

import com.mawen.learn.nacos.client.logger.Level;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/18
 */
public class Log4jActivateOption extends AbstractActiveOption {

	protected Logger logger;

	public Log4jActivateOption(Logger logger) {
		this.logger = logger;
	}

	@Override
	public void activateConsoleAppender(String target, String encoding) {
		ConsoleAppender appender = new ConsoleAppender();
		// TODO
	}

	@Override
	public void activateAppender(String productName, String file, String encoding) {

	}

	@Override
	public void activateAsyncAppender(String productName, String file, String encoding) {

	}

	@Override
	public void activateAsyncAppender(String productName, String file, String encoding, int queueSize, int discardingThreshold) {

	}

	@Override
	public void activateAppenderWithTimeAndSizeRolling(String productName, String file, String encoding, String size) {

	}

	@Override
	public void activateAppenderWithTimeAndSizeRolling(String productName, String file, String encoding, String size, String datePattern) {

	}

	@Override
	public void activateAppenderWithTimeAndSizeRolling(String productName, String file, String encoding, String size, String datePattern, int maxBackupIndex) {

	}

	@Override
	public void activateAppenderWithSizeRolling(String productName, String file, String encoding, String size, int maxBackupIndex) {

	}

	@Override
	public void activateAsync(int queueSize, int discardingThreshold) {

	}

	@Override
	public void activateAsync(List<Object[]> args) {

	}

	@Override
	public void activateAppender(com.mawen.learn.nacos.client.logger.Logger logger) {

	}

	@Override
	public void setLevel(Level level) {

	}

	@Override
	public void setAdditivity(boolean additivity) {

	}
}

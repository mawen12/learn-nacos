package com.mawen.learn.nacos.client.logger.option;

import java.util.List;

import com.mawen.learn.nacos.client.logger.Level;

/**
 * 激活Logger的选项，请参考具体的实现逻辑：
 * <ul>
 *     <li>Appender/Layout</li>
 *     <li>Level</li>
 *     <li>Additivity</li>
 *     <li>Async</li>
 * </ul>
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/15
 */
public interface ActivateOption {

	/**
	 * 设置 ConsoleAppender，生产环境慎用
	 *
	 * @param target   System.out or System.err
	 * @param encoding 编码
	 */
	void activateConsoleAppender(String target, String encoding);

	/**
	 * 设置 FileAppender，日志按天回滚
	 *
	 * @param productName 产品名，如nacos
	 * @param file        日志文件名，如nacos.log，支持子目录，如client/nacos.log
	 * @param encoding    编码
	 */
	void activateAppender(String productName, String file, String encoding);

	/**
	 * 设置 AsyncAppender，内嵌 DailyRollingFileAppender，日志按天回滚，参考：{@link ActivateOption#activateAsync(int, int)}
	 *
	 * @param productName 产品名，如nacos
	 * @param file        日志文件名，如nacos.log，支持子目录，如client/nacos.log
	 * @param encoding    编码
	 */
	void activateAsyncAppender(String productName, String file, String encoding);

	/**
	 * 设置 AsyncAppender，内嵌DailyRollingFileAppender，日志按天回滚，参考 {@link ActivateOption#activateAsync(int, int)}
	 *
	 * @param productName         产品名，如nacos
	 * @param file                日志文件名，如nacos.log，支持子目录，如client/nacos.log
	 * @param encoding            编码
	 * @param queueSize           等待队列大小
	 * @param discardingThreshold 该参数仅对logback实现有效，log4j和log4j2无效
	 */
	void activateAsyncAppender(String productName, String file, String encoding, int queueSize, int discardingThreshold);

	/**
	 * 设置按天和文件大小会滚
	 *
	 * @param productName 产品名，如nacos
	 * @param file        日志文件名，如nacos.log，支持子目录，如client/nacos.log
	 * @param encoding    编码
	 * @param size        文件大小，如300MB，支持KB、MB、GB，该参数对log4j实现不生效，log4j2和logback有效
	 */
	void activateAppenderWithTimeAndSizeRolling(String productName, String file, String encoding, String size);

	/**
	 * 设置按日期格式和文件大小回滚
	 * 说明：Log4j 对日期格式不生效，只有按大小回滚，同时不支持备份文件，即达到文件大小直接截断，如果需要备份文件夹，请参考带 maxBackupIndex 参数的方法
	 *
	 * @param productName 产品名，如nacos
	 * @param file        日志文件名，如nacos.log，支持子目录，如client/nacos.log
	 * @param encoding    编码
	 * @param size        文件大小，如300MB，支持KB、MB、GB，该参数对log4j实现不生效，log4j2和logback有效
	 * @param datePattern 日期格式，如yyyy-MM-dd 或 yyyy-MM，请自行保证格式正确，该参数对log4j实现不生效，log4j2和logback有效
	 */
	void activateAppenderWithTimeAndSizeRolling(String productName, String file, String encoding, String size, String datePattern);

	/**
	 * 设置按日期格式、文件大小、最大备份文件数回滚
	 * 说明：
	 * - Log4j 对日期格式不起效，只有按大小、备份文件数 maxBackupIndex 参数必须是 >= 0 的证书，为0时表示直接截断，不备份
	 * - 备份格式说明：
	 * - Log4j: notify.log.1, notify.log.2， 即备份文件以 .1 .2结尾，序号从1开始
	 * - Logback: notify.log.2014-09-19.0, notify.log.2014-09-19.1，即中间会带日期格式，同时序号从0开始
	 *
	 * @param productName    产品名，如nacos
	 * @param file           日志文件名，如nacos.log，支持子目录，如client/nacos.log
	 * @param encoding       编码
	 * @param size           文件大小，如300MB，支持KB，MB，GB
	 * @param datePattern    日期格式，如yyyy-MM-dd 或 yyyy-MM，请自行保证格式正确，该参数对log4j实现不生效，log4j2和logback有效
	 * @param maxBackupIndex 最大备份文件数，如10（对于 Logback，则是保留10天的文件，但是这10天内的文件则会按大小回滚）
	 */
	void activateAppenderWithTimeAndSizeRolling(String productName, String file, String encoding, String size, String datePattern, int maxBackupIndex);

	/**
	 * 设置按文件大小、最大备份数回滚
	 * 说明：
	 * - Log4j 备份文件数 maxBackupIndex 参数必须是 >= 0 的证书，为0时表示直接截断，不备份
	 * - 备份格式说明：
	 * - Log4j: notify.log.1, notify.log.2， 即备份文件以 .1 .2结尾，序号从1开始
	 * - Logback: notify.log.1, notify.log.2
	 *
	 * @param productName    产品名，如nacos
	 * @param file           日志文件名，如nacos.log，支持子目录，如client/nacos.log
	 * @param encoding       编码
	 * @param size           文件大小，如300MB，支持KB，MB，GB
	 * @param maxBackupIndex 最大备份文件数，如10
	 */
	void activateAppenderWithSizeRolling(String productName, String file, String encoding, String size, int maxBackupIndex);

	/**
	 * 将当前 logger 对象的 appender 设置为异步的 Appender
	 * 注意：此 logger 需要提前进行 Appender 的初始化
	 *
	 * @param queueSize           等待队列大小
	 * @param discardingThreshold 该参数仅对logback实现有效，log4j和log4j2无效
	 */
	void activateAsync(int queueSize, int discardingThreshold);

	/**
	 * 将当前 logger 对象的 appender 设置为异步的 Appender
	 * 注意：此 logger 需要提前进行 Appender 的初始化
	 *
	 * @param args AsyncAppender 配置参数，请自行保证参数的正确性，要求每个Object[]有三个元素，第一个为set方法名，第二个为方法类型数组，第三个为对应的参数值，
	 *             如 args.add(new Object[]{"setBufferSize", new Class<?>[]{int.class}, queueSize});
	 */
	void activateAsync(List<Object[]> args);

	/**
	 * 使用 logger 对应的 appender 来初始化当前 logger
	 */
//	void activateAppender(Logger logger);

	/**
	 * 设置日志级别
	 *
	 * @param level 日志级别
	 */
	void setLevel(Level level);

	/**
	 * 获取日志级别
	 */
	Level getLevel();

	/**
	 * 设置日志是否 Attach 到 Parent
	 */
	void setAdditivity(boolean additivity);

	/**
	 * 获取所属的产品名
	 *
	 * @return 所属的产品名
	 */
	String getProductName();
}

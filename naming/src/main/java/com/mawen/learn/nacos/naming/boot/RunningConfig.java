package com.mawen.learn.nacos.naming.boot;

import javax.servlet.ServletContext;

import com.mawen.learn.nacos.naming.raft.RaftCore;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/24
 */
@Slf4j
@Component
public class RunningConfig implements ApplicationListener<WebServerInitializedEvent> {

	private static int serverPort;

	private static String contextPath;

	@Autowired
	private ServletContext servletContext;

	@Override
	public void onApplicationEvent(WebServerInitializedEvent evnet) {
		log.info("got port: {}", evnet.getWebServer().getPort());
		log.info("got path: {}", servletContext.getContextPath());

		serverPort = evnet.getWebServer().getPort();
		contextPath = servletContext.getContextPath();

		try {
			RaftCore.init();
		}
		catch (Exception e) {
			log.error("failed to initialize raft sub system", e);
		}
	}

	public static int getServerPort() {
		return serverPort;
	}

	public static String getContextPath() {
		return contextPath;
	}
}

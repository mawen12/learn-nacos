package com.mawen.learn.nacos.client.naming.core;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

import com.alibaba.fastjson.JSON;
import com.mawen.learn.nacos.common.util.IoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/19
 */
public class PushRecver implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(PushRecver.class);

	public static final int UDP_MSS = 64 * 1024;

	private ScheduledExecutorService executorService;

	private DatagramSocket udpSocket;

	private HostReactor hostReactor;

	public PushRecver(HostReactor hostReactor) {
		try {
			this.hostReactor = hostReactor;
			this.udpSocket = new DatagramSocket();

			this.executorService = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
				@Override
				public Thread newThread(Runnable r) {
					Thread thread = new Thread(r);
					thread.setDaemon(true);
					thread.setName("com.mawen.learn.nacos.naming.receiver");
					return thread;
				}
			});

			this.executorService.execute(this);
		}
		catch (Exception e) {
			log.error("NA init udp socket failed", e);
		}
	}

	@Override
	public void run() {
		while (true) {

			try {
				byte[] buffer = new byte[UDP_MSS];
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

				udpSocket.receive(packet);

				String json = new String(IoUtils.tryDecompress(packet.getData()), "UTF-8").trim();
				log.info("received push data: {} from {}", json, packet.getAddress().toString());

				PushPacket pushPacket = JSON.parseObject(json, PushPacket.class);
				String ack;
				if ("dom".equals(pushPacket.type)) {
					// dom update
					hostReactor.processDomJSON(pushPacket.data);

					// send ack to server

				}
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}



	public int getUDPPort() {
		return udpSocket.getLocalPort();
	}

	public static class PushPacket {
		public String type;
		public long lastRefTime;
		public String data;

	}
}

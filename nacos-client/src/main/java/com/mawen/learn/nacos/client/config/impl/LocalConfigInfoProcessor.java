package com.mawen.learn.nacos.client.config.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.mawen.learn.nacos.client.config.common.Constants;
import com.mawen.learn.nacos.client.config.utils.JVMUtil;
import com.mawen.learn.nacos.client.config.utils.SnapShotSwitch;
import com.mawen.learn.nacos.client.utils.ConcurrentDiskUtil;
import com.mawen.learn.nacos.common.util.IoUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/21
 */
public class LocalConfigInfoProcessor {

	private static final Logger log = LoggerFactory.getLogger(LocalConfigInfoProcessor.class);

	public static final String LOCAL_FILEROOT_PATH;

	public static final String LOCAL_SNAPSHOT_PATH;

	static {
		LOCAL_FILEROOT_PATH = System.getProperty("JM.LOG.PATH", System.getProperty("user.home")) +
				File.separator + "nacos" + File.separator + "config";
		LOCAL_SNAPSHOT_PATH = System.getProperty("JM.SNAPSHOT.PATH", System.getProperty("user.home")) +
				File.separator + "nacos" + File.separator + "config";
		log.warn("LOCAL_SHAPSHOT_PATH: {}", LOCAL_SNAPSHOT_PATH);
	}

	public static String getFailover(String serverName, String dataId, String group, String tenant) {
		File localPath = getFailoverFile(serverName, dataId, group, tenant);
		if (!localPath.exists() || !localPath.isFile()) {
			return null;
		}

		try {
			return readFile(localPath);
		}
		catch (IOException e) {
			log.error("get failover error, {}", localPath, e);
			return null;
		}
	}

	public static String getSnapshot(String name, String dataId, String group, String tenant) {
		if (!SnapShotSwitch.getIsSnapShot()) {
			return null;
		}
		File file = getSnapshotFile(name, dataId, group, tenant);
		if (!file.exists() || !file.isFile()) {
			return null;
		}

		try {
			return readFile(file);
		}
		catch (IOException e) {
			log.error("get snapshot error, {}", file, e);
			return null;
		}
	}

	public static void saveSnapshot(String envName, String dataId, String group, String tenant, String config) {
		if (!SnapShotSwitch.getIsSnapShot()) {
			return;
		}

		File file = getSnapshotFile(envName, dataId, group, tenant);
		if (null == config) {
			try {
				IoUtils.delete(file);
			}
			catch (IOException e) {
				log.error("delete snapshot error, {}", file, e);
			}
		}
		else {
			try {
				boolean isMdOk = file.getParentFile().mkdirs();
				if (!isMdOk) {
					log.error("save snapshot error");
				}
				if (JVMUtil.isMultiInstance()) {
					ConcurrentDiskUtil.writeFileContent(file, config, Constants.ENCODE);
				}
				else {
					IoUtils.writeStringToFile(file, config, Constants.ENCODE);
				}
			}
			catch (IOException e) {
				log.error("save snapshot error, {}", file, e);
			}
		}
	}

	public static void cleanAllSnapshot() {
		try {
			File rootFile = new File(LOCAL_SNAPSHOT_PATH);
			File[] files = rootFile.listFiles();
			if (files == null || files.length == 0) {
				return;
			}

			for (File file : files) {
				if (file.getName().endsWith("_nacos")) {
					IoUtils.cleanDirectory(file);
				}
			}
		}
		catch (IOException e) {
			log.error("clean all snapshot error", e);
		}
	}

	public static void cleanEnvSnapshot(String envName) {
		File tmp = new File(LOCAL_SNAPSHOT_PATH, envName + "_nacos");
		tmp = new File(tmp, "snapshot");
		try {
			IoUtils.cleanDirectory(tmp);
			log.info("success delete {}-snapshot", envName);
		}
		catch (IOException e) {
			log.error("fail delete {}-snapshot error", envName, e);
			e.printStackTrace();
		}
	}

	static File getSnapshotFile(String envName, String dataId, String group, String tenant) {
		File tmp = new File(LOCAL_SNAPSHOT_PATH, envName + "_nacos");
		if (StringUtils.isBlank(tenant)) {
			tmp = new File(tmp, "snapshot");
		}
		else {
			tmp = new File(tmp, "snapshot-tenant");
			tmp = new File(tmp, tenant);
		}

		return new File(new File(tmp, group), dataId);
	}

	static File getFailoverFile(String serverName, String dataId, String group, String tenant) {
		File tmp = new File(LOCAL_SNAPSHOT_PATH, serverName + "_nacos");
		tmp = new File(tmp, "data");
		if (StringUtils.isBlank(tenant)) {
			tmp = new File(tmp, "config-data");
		}
		else {
			tmp = new File(tmp, "config-data-tenant");
			tmp = new File(tmp, tenant);
		}

		return new File(new File(tmp, group), dataId);
	}

	private static String readFile(File file) throws IOException {
		if (!file.exists() || !file.isFile()) {
			return null;
		}

		if (JVMUtil.isMultiInstance()) {
			return ConcurrentDiskUtil.getFileContent(file, Constants.ENCODE);
		}
		else {
			InputStream is = null;
			try {
				is = new FileInputStream(file);
				return IoUtils.toString(is, Constants.ENCODE);
			}
			finally {
				try {
					if (is != null) {
						is.close();
					}
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}

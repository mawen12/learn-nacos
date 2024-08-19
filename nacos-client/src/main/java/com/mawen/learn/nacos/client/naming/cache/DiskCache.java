package com.mawen.learn.nacos.client.naming.cache;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.mawen.learn.nacos.api.naming.pojo.Instance;
import com.mawen.learn.nacos.client.naming.core.Domain;
import com.mawen.learn.nacos.client.naming.utils.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/19
 */
public class DiskCache {

	private static final Logger log = LoggerFactory.getLogger(DiskCache.class);

	public static void write(Domain dom, String dir) {

		try {
			makeSureCacheDirExists(dir);

			File file = new File(dir, dom.getKey());
			if (!file.exists()) {
				// add another !file.exists() to avoid conflicted creating-new-file from multi-instances
				if (!file.createNewFile() && !file.exists()) {
					throw new IllegalStateException("failed to create cache file");
				}
			}

			StringBuilder keyContentBuffer = new StringBuilder("");

			String json = dom.getJsonFromServer();

			if (StringUtils.isEmpty(json)) {
				json = JSON.toJSONString(dom);
			}

			// Use the concurrent API to ensure the consistency.
			keyContentBuffer.append(json);

			ConcurrentDiskUtil.writeFileContent(file, keyContentBuffer.toString(), Charset.defaultCharset().toString());
		}
		catch (Throwable e) {
			log.error("NA failed to write cache from dom {}", dom.getName(), e);
		}
	}

	public static String getLineSeparator() {
		return System.getProperty("line.separator");
	}

	public static Map<String, Domain> read(String cacheDir) {
		Map<String, Domain> domMap = new HashMap<>(16);

		BufferedReader reader = null;
		try {
			File[] files = makeSureCacheDirExists(cacheDir).listFiles();
			if (files == null) {
				return domMap;
			}

			for (File file : files) {
				if (!file.isFile()) {
					continue;
				}

				if (!(file.getName().endsWith(Domain.SPLITTER + "meta") || file.getName().endsWith(Domain.SPLITTER + "special-url"))) {
					Domain dom = new Domain(file.getName());
					List<Instance> ips = new ArrayList<>();
					dom.setHosts(ips);

					Domain newFormat = null;

					try {
						String dataString = ConcurrentDiskUtil.getFileContent(file, Charset.defaultCharset().toString());
						reader = new BufferedReader(new StringReader(dataString));

						String json;
						while ((json = reader.readLine()) != null) {
							try {
								if (!json.startsWith("{")) {
									continue;
								}

								newFormat = JSON.parseObject(json, Domain.class);

								if (StringUtils.isEmpty(newFormat.getName())) {
									ips.add(JSON.parseObject(json, Instance.class));
								}
							}
							catch (Throwable e) {
								log.error("NA error while parsing cache file: {}", json, e);
							}
						}
					}
					catch (Exception e) {
						log.error("NA failed to read cache from dom: {}", file.getName(), e);
					}
					finally {
						try {
							try {
								if (reader != null) {
									reader.close();
								}
							}
							catch (IOException e) {
								// ignored
							}
						}
					}

					if (newFormat != null && !StringUtils.isEmpty(newFormat.getName()) && !CollectionUtils.isEmpty(newFormat.getHosts())) {
						domMap.put(dom.getKey(), newFormat);
					}
					else if (!CollectionUtils.isEmpty(dom.getHosts())) {
						domMap.put(dom.getKey(), dom);
					}
				}
			}
		}
		catch (Throwable e) {
			log.error("NA failed to read cache file", e);
		}

		return domMap;
	}

	private static File makeSureCacheDirExists(String dir) {
		File cacheDir = new File(dir);
		if (!cacheDir.exists() && !cacheDir.mkdirs()) {
			throw new IllegalStateException("failed to create cache dir: " + dir);
		}

		return cacheDir;
	}
}

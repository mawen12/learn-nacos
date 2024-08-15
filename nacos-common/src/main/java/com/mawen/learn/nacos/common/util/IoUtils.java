package com.mawen.learn.nacos.common.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/15
 */
public class IoUtils {

	private static final String DEFAULT_CHARSET = "UTF-8";

	public static String toString(InputStream input, String encoding) throws IOException {
		encoding = encoding == null ? DEFAULT_CHARSET : encoding;
		return toString(new InputStreamReader(input, encoding));
	}

	public static String toString(Reader reader) throws IOException {
		CharArrayWriter sw = new CharArrayWriter();
		copy(reader, sw);
		return sw.toString();
	}

	public static long copy(Reader input, Writer output) throws IOException {
		char[] buffer = new char[1 << 12];
		int bytesRead;
		long totalBytes = 0;
		while ((bytesRead = input.read(buffer)) >= 0) {
			output.write(buffer, 0, bytesRead);
			totalBytes += bytesRead;
		}
		return totalBytes;
	}

	public static long copy(InputStream input, OutputStream output) throws IOException {
		byte[] buffer = new byte[1024];
		int bytesRead;
		int totalBytes = 0;
		while ((bytesRead = input.read(buffer)) != -1) {
			output.write(buffer, 0, bytesRead);
			totalBytes += bytesRead;
		}
		return totalBytes;
	}

	public static List<String> readLines(Reader input) throws IOException {
		BufferedReader reader = toBufferedReader(input);
		List<String> list = new ArrayList<>();
		String line;
		while ((line = reader.readLine()) != null) {
			if (StringUtils.isNotEmpty(line)) {
				list.add(line.trim());
			}
		}
		return list;
	}

	private static BufferedReader toBufferedReader(Reader reader) {
		return reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
	}

	public static boolean delete(File fileOrDir) throws IOException {
		if (fileOrDir == null) {
			return false;
		}

		if (fileOrDir.isDirectory()) {
			cleanDirectory(fileOrDir);
		}

		return fileOrDir.delete();
	}

	public static void cleanDirectory(File directory) throws IOException {
		if (!directory.exists()) {
			String message = directory + " does not exist";
			throw new IllegalArgumentException(message);
		}

		if (!directory.isDirectory()) {
			String message = directory + " is not a directory";
			throw new IllegalArgumentException(message);
		}

		File[] files = directory.listFiles();
		if (files == null) {
			String message = "Failed to list contents of " + directory;
			throw new IOException(message);
		}

		IOException exception = null;
		for (File file : files) {
			try {
				delete(file);
			}
			catch (IOException ioe) {
				exception = ioe;
			}
		}

		if (null != exception) {
			throw exception;
		}
	}

	public static void writeStringToFile(File file, String data, String encoding) throws IOException {
		OutputStream os = null;
		try {
			os = new FileOutputStream(file);
			os.write(data.getBytes(encoding));
			os.flush();
		}
		finally {
			if (os != null) {
				os.close();
			}
		}
	}

	public static byte[] tryDecompress(InputStream raw) {
		try {
			GZIPInputStream gis = new GZIPInputStream(raw);
			ByteArrayOutputStream out = new ByteArrayOutputStream();

			IoUtils.copy(gis, out);

			return out.toByteArray();
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}

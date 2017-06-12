package com.magic.util;

import java.io.File;

public class Util {

	public static int getFileId(File file) {
		String name = file.getName();
		int idx = name.indexOf('.');
		int val = Integer.parseInt(name.substring(0, idx));
		return val;
	}

	/**
	 * 使用unix时间戳（秒）
	 */
	public static int getFileId() {
		return (int) (System.currentTimeMillis() / 1000L);
	}

	/**
	 * Create-if-not-exists for directory or fail
	 */
	public static void ensuredir(File dirname) {
		if (dirname.exists() && dirname.isDirectory())
			return;
		if (!dirname.mkdirs())
			throw new RuntimeException("cannot create " + dirname);
	}

	/**
	 * Create-if-not-exists for directory or fail
	 */
	public static void ensureFile(File fileName) {
		if (fileName.exists() && fileName.isFile()) {
			return;
		} else {
			throw new RuntimeException("cannot read " + fileName);
		}
	}

	/**
	 * Given a directory and a timestamp, construct a data file name.
	 */
	public static File makeDataFilename(File dirname, int tstamp) {
		return new File(dirname, "" + tstamp + ".bitcask.data");
	}

	/**
	 * Given a directory and a timestamp, construct a hint file name.
	 */
	public static File makeHintFilename(File dirname, int tstamp) {
		return new File(dirname, "" + tstamp + ".bitcask.hint");
	}

}

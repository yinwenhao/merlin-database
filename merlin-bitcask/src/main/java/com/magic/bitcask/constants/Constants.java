package com.magic.bitcask.constants;

public class Constants {

	/**
	 * 时间戳（毫秒）左移10位，加上分片id，组成version
	 */
	public static final int TIME_SHIFT_FOR_VERSION = 10;

	/**
	 * 过期时间——默认0，表示不限制
	 */
	public static final long EXPIRE_TIME_DEFAULT = 0;

}

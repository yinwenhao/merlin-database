package com.magic.netty.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.magic.constants.Constants;
import com.magic.util.CRC16;

public class Request {

	@JsonIgnore
	private String guid;

	private long version;

	private long expire;

	private String method;

	private String key;

	private String value;

	public Request() {
	}

	/**
	 * 这个用作心跳、before_set指令，这个version=0，expire=0
	 * 
	 * @param method
	 */
	public Request(String method) {
		this(method, "", "", 0, Constants.EXPIRE_TIME_DEFAULT);
	}

	/**
	 * 这个用作get指令，这个version=0，expire=0
	 * 
	 * @param method
	 * @param key
	 */
	public Request(String method, String key) {
		this(method, key, "", 0, Constants.EXPIRE_TIME_DEFAULT);
	}

	/**
	 * 这个用作delete指令，这个会生成version，expire=0
	 * 
	 * @param method
	 * @param key
	 * @param value
	 */
	public Request(String method, String key, String value) {
		this(method, key, value, Constants.EXPIRE_TIME_DEFAULT);
	}

	/**
	 * 这个用作set指令，这个会生成version
	 * 
	 * @param method
	 * @param key
	 * @param value
	 * @param expire
	 */
	public Request(String method, String key, String value, long expire) {
		this(method, key, value, newVersion(key, value), expire);
	}

	public Request(String method, String key, String value, long version, long expire) {
		this.version = version;
		this.expire = expire;
		this.key = key;
		this.value = value;
		this.method = method;
		this.guid = normalGuid();
	}

	private static long newVersion(String key, String value) {
		CRC16 crc16 = new CRC16();
		crc16.update(key);
		crc16.update(value);
		return (System.currentTimeMillis() << Constants.TIME_SHIFT_FOR_VERSION) ^ crc16.getValue();
	}

	private String normalGuid() {
		return this.key + this.method + this.version;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	public String getGuid() {
		if (guid == null) {
			guid = normalGuid();
		}
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public long getExpire() {
		return expire;
	}

	public void setExpire(long expire) {
		this.expire = expire;
	}

}

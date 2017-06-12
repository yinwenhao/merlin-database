package com.magic.bitcask.entity;

public class BitCaskValue {

	private long version;

	private long expire;

	private String key;

	private String value;

	public BitCaskValue() {
	}

	public BitCaskValue(String key) {
		this.version = 0;
		this.expire = 0;
		this.key = key;
		this.value = null;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
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

	public long getExpire() {
		return expire;
	}

	public void setExpire(long expire) {
		this.expire = expire;
	}

}

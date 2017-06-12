package com.magic.bitcask.entity;

import java.nio.ByteBuffer;

public class BitCaskRecord {

	private int crc;

	private long version;

	private long expire; // 过期时间

	private short keySize;

	private int valueSize;

	private String key;

	private String value;

	// 这个是转化后的buffer
	private ByteBuffer[] buffers;

	public BitCaskRecord(int crc, long version, long expire, short keySize, int valueSize, String key, String value,
			ByteBuffer[] buffers) {
		this.crc = crc;
		this.version = version;
		this.expire = expire;
		this.keySize = keySize;
		this.valueSize = valueSize;
		this.key = key;
		this.value = value;
		this.buffers = buffers;
	}

	public int getCrc() {
		return crc;
	}

	public void setCrc(int crc) {
		this.crc = crc;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	public short getKeySize() {
		return keySize;
	}

	public void setKeySize(short keySize) {
		this.keySize = keySize;
	}

	public int getValueSize() {
		return valueSize;
	}

	public void setValueSize(int valueSize) {
		this.valueSize = valueSize;
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

	public ByteBuffer[] getBuffers() {
		return buffers;
	}

	public void setBuffers(ByteBuffer[] buffers) {
		this.buffers = buffers;
	}

	public long getExpire() {
		return expire;
	}

	public void setExpire(long expire) {
		this.expire = expire;
	}

}

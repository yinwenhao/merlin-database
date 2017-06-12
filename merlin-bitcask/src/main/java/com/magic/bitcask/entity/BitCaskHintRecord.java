package com.magic.bitcask.entity;

public class BitCaskHintRecord {

	private int crc32;

	// 32，不包括fileId
	private BitCaskKey bck;

	private short keySize;

	private String key;

	public int getCrc32() {
		return crc32;
	}

	public void setCrc32(int crc32) {
		this.crc32 = crc32;
	}

	public BitCaskKey getBck() {
		return bck;
	}

	public void setBck(BitCaskKey bck) {
		this.bck = bck;
	}

	public short getKeySize() {
		return keySize;
	}

	public void setKeySize(short keySize) {
		this.keySize = keySize;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

}

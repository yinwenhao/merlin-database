package com.magic.bitcask.entity;

public class BitCaskKey {

	private int fileId;

	private long version;

	private long expire;

	private long position;

	private int size;

	private int crc32;

	public BitCaskKey(int fileId, long version, long expire, long position, int size, int crc32) {
		this.fileId = fileId;
		this.version = version;
		this.expire = expire;
		this.position = position;
		this.size = size;
		this.crc32 = crc32;
	}

	public boolean isNewerThan(BitCaskKey old) {
		return old.getVersion() < this.getVersion();
	}

	public int getFileId() {
		return fileId;
	}

	public void setFileId(int fileId) {
		this.fileId = fileId;
	}

	public long getPosition() {
		return position;
	}

	public void setPosition(long position) {
		this.position = position;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getCrc32() {
		return crc32;
	}

	public void setCrc32(int crc32) {
		this.crc32 = crc32;
	}

	public long getVersion() {
		return version;
	}

	public long getExpire() {
		return expire;
	}

	public void setExpire(long expire) {
		this.expire = expire;
	}

}

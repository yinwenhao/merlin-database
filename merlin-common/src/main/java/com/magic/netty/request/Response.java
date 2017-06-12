package com.magic.netty.request;

import com.magic.util.CRC32;

public class Response {

	private String guid;

	private long version;

	private String key;

	private String value;

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

	@Override
	public boolean equals(Object o) {
		if (o instanceof Response) {
			Response r = (Response) o;
			if (stringEqualsOrBothNull(r.getGuid(), guid) && r.getVersion() == version
					&& stringEqualsOrBothNull(r.getKey(), key) && stringEqualsOrBothNull(r.getValue(), value)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		CRC32 crc32 = new CRC32();
		crc32.update(version);
		if (key != null) {
			crc32.update(key);
		}
		if (value != null) {
			crc32.update(value);
		}
		return crc32.getValue();
	}

	private boolean stringEqualsOrBothNull(String s1, String s2) {
		if (s1 == null) {
			if (s2 == null) {
				return true;
			}
		} else if (s1.equals(s2)) {
			return true;
		}
		return false;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

}

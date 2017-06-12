package com.magic.synchronize;

public interface Synchronizer {

	public static final String SPLIT = ",";

	public static final String NULL = "null";

	public void start() throws Exception;

	public void shutdown() throws Exception;

	public void checkHash(String keyCrc32, String hashString) throws Exception;

	public void set(String key, String value, long version, long expire) throws Exception;

	public void needValue(String keyCrc32) throws Exception;

}

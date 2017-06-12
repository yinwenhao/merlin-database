package com.magic.bitcask.core;

import com.magic.bitcask.entity.BitCaskKey;

public interface BitCaskKeydir {

	public boolean put(String key, BitCaskKey record);

	public BitCaskKey get(String key);

	public boolean waitForReady(int timeoutSecs);

	public boolean isReady();

	public void markReady();

}

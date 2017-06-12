package com.magic.bitcask.core;

import java.nio.ByteBuffer;

public interface RecordIterator {

	public void each(String key, int crc32, ByteBuffer[] bb) throws Exception;

}

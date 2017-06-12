package com.magic.bitcask.core;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.magic.bitcask.entity.BitCaskKey;
import com.magic.bitcask.entity.BitCaskValue;

public interface BitCaskFile {

	/**
	 * 读数据
	 * 
	 * @param offset
	 * @param length
	 * @return
	 * @throws IOException
	 */
	public BitCaskValue read(long offset, int length) throws IOException;

	public BitCaskKey write(String key, String value, long version, long expire) throws IOException;

	public File getFile();

	public boolean needCreateNewFile(String key, String value, long maxFileSize);

	public boolean needCreateNewFile(int keyLength, int valueLength, long maxFileSize);

	public void closeWriting() throws IOException;

	public void doForEachKey(KeyIterator iter) throws Exception;

	public void close() throws IOException;

	public void doForEachRecord(RecordIterator iter) throws Exception;

	public void writeByteBufferAndUpdateKey(BitCaskKey bck, ByteBuffer[] bb) throws IOException;

	public boolean isWriteable();

}

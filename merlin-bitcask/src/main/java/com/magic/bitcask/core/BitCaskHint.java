package com.magic.bitcask.core;

import java.io.File;
import java.io.IOException;

import com.magic.bitcask.entity.BitCaskKey;

public interface BitCaskHint {

	public void write(String key, BitCaskKey bck) throws IOException;

	public File getFile();

	public void closeWriting() throws IOException;

	public void doForEachKey(KeyIterator iter) throws Exception;

	public void close() throws IOException;

}

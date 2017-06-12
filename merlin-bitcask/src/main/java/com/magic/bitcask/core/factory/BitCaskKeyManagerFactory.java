package com.magic.bitcask.core.factory;

import java.io.File;
import java.io.IOException;

import com.magic.bitcask.core.BitCaskKeydir;

public interface BitCaskKeyManagerFactory {

	public BitCaskKeydir createBitCaskKeyManager(File dirname, int openTimeoutSecs) throws IOException;

}

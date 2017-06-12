package com.magic.bitcask.core.factory;

import java.io.File;

import com.magic.bitcask.core.BitCaskServer;
import com.magic.bitcask.options.BitCaskOptions;

public interface BitCaskFactory {

	public BitCaskServer createBitCask(File dirname, BitCaskOptions opts) throws Exception;

}

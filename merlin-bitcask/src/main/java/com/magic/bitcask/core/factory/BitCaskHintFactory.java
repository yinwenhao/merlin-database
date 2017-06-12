package com.magic.bitcask.core.factory;

import java.io.File;
import java.io.IOException;

import com.magic.bitcask.core.BitCaskHint;

public interface BitCaskHintFactory {

	public BitCaskHint openBitCaskHint(File dirname) throws IOException;

	public BitCaskHint createBitCaskHint(File dirname, int fileId) throws IOException;

}

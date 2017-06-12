package com.magic.bitcask.core.factory.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import com.magic.bitcask.core.BitCaskHint;
import com.magic.bitcask.core.factory.BitCaskHintFactory;
import com.magic.bitcask.core.impl.BitCaskHintImpl;
import com.magic.util.Util;

public class BitCaskHintFactoryImpl implements BitCaskHintFactory {

	@SuppressWarnings("resource")
	@Override
	public BitCaskHint openBitCaskHint(File fileName) throws IOException {
		Util.ensureFile(fileName);

		FileChannel rch = new RandomAccessFile(fileName, "r").getChannel();

		BitCaskHintImpl result = new BitCaskHintImpl(fileName);
		result.initForRead(rch);
		return result;
	}

	@SuppressWarnings("resource")
	@Override
	public BitCaskHint createBitCaskHint(File dirname, int fileId) throws IOException {
		Util.ensuredir(dirname);

		File fileName = Util.makeHintFilename(dirname, fileId);
		if (!fileName.createNewFile()) {
			throw new IOException("create hint file error: " + fileName.getAbsolutePath());
		}

		FileChannel wch = new FileOutputStream(fileName, true).getChannel();

		BitCaskHintImpl result = new BitCaskHintImpl(fileName);
		result.initForWrite(wch);
		return result;
	}

}

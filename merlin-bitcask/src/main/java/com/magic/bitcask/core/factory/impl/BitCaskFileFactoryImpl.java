package com.magic.bitcask.core.factory.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import com.magic.bitcask.core.BitCaskFile;
import com.magic.bitcask.core.factory.BitCaskFileFactory;
import com.magic.bitcask.core.impl.BitCaskFileImpl;
import com.magic.util.Util;

public class BitCaskFileFactoryImpl implements BitCaskFileFactory {

	@SuppressWarnings("resource")
	@Override
	public BitCaskFile openBitCaskFile(File fileName) throws IOException {
		Util.ensureFile(fileName);

		FileChannel rch = new RandomAccessFile(fileName, "r").getChannel();

		return new BitCaskFileImpl(fileName, rch);
	}

	@SuppressWarnings("resource")
	@Override
	public BitCaskFile openBitCaskFileForWrite(File fileName) throws IOException {
		Util.ensureFile(fileName);

		FileChannel wch = new FileOutputStream(fileName, true).getChannel();
		FileChannel rch = new RandomAccessFile(fileName, "r").getChannel();

		return new BitCaskFileImpl(fileName, wch, rch);
	}

	@Override
	public BitCaskFile createBitCaskFile(File dirname) throws IOException {
		return createBitCaskFile(dirname, Util.getFileId());
	}

	@SuppressWarnings("resource")
	@Override
	public BitCaskFile createBitCaskFile(File dirname, int fileId) throws IOException {
		Util.ensuredir(dirname);

		boolean created = false;

		File filename = null;
		while (!created) {
			filename = Util.makeDataFilename(dirname, fileId);
			created = filename.createNewFile();
			if (!created) {
				fileId += 1;
			}
		}

		FileChannel wch = new FileOutputStream(filename, true).getChannel();

		FileChannel rch = new RandomAccessFile(filename, "r").getChannel();

		return new BitCaskFileImpl(filename, wch, rch);
	}

	@SuppressWarnings("resource")
	@Override
	public BitCaskFile createBitCaskHintFile(File dirname, int fileId) throws IOException {
		Util.ensuredir(dirname);

		File filename = Util.makeDataFilename(dirname, fileId);
		if (!filename.createNewFile()) {
			throw new IOException("create hint file error: " + filename.getAbsolutePath());
		}

		FileChannel wch = new FileOutputStream(filename, true).getChannel();

		FileChannel rch = new RandomAccessFile(filename, "r").getChannel();

		return new BitCaskFileImpl(filename, wch, rch);
	}

}

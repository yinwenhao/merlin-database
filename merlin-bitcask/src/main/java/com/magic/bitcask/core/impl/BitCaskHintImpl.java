package com.magic.bitcask.core.impl;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.magic.bitcask.core.BitCaskHint;
import com.magic.bitcask.core.KeyIterator;
import com.magic.bitcask.entity.BitCaskKey;
import com.magic.bitcask.exception.IterException;
import com.magic.util.CRC32;
import com.magic.util.IO;
import com.magic.util.Util;

public class BitCaskHintImpl implements BitCaskHint {

	private Logger log = LoggerFactory.getLogger(getClass());

	// 4 + 4 + 8 + 8 + 8 + 4 + 4 + 2
	private static final int HEADER_SIZE = 38;

	private static final String CHARSET = "UTF-8";

	private FileChannel wch;
	private FileChannel rch;

	private final File file;
	private final int fileId;

	public BitCaskHintImpl(File file) {
		this.fileId = Util.getFileId(file);
		this.file = file;
	}

	public void initForWrite(FileChannel wch) {
		this.wch = wch;
	}

	public void initForRead(FileChannel rch) {
		this.rch = rch;
	}

	@Override
	public File getFile() {
		return file;
	}

	@Override
	public void write(String key, BitCaskKey bck) throws IOException {
		byte[] header = new byte[HEADER_SIZE];
		ByteBuffer h = ByteBuffer.wrap(header);

		byte[] keyBytes = key.getBytes(CHARSET);
		ByteBuffer k = ByteBuffer.wrap(keyBytes).asReadOnlyBuffer();

		h.putLong(4, bck.getVersion());
		h.putLong(12, bck.getExpire());
		h.putLong(20, bck.getPosition());
		h.putInt(28, bck.getSize());
		h.putInt(32, bck.getCrc32());
		h.putShort(36, (short) keyBytes.length);

		CRC32 crc = new CRC32();
		crc.update(header, 4, HEADER_SIZE - 4);
		crc.update(key);
		int crcValue = crc.getValue();

		h.putInt(0, crcValue);

		ByteBuffer[] vec = new ByteBuffer[] { h, k };
		IO.writeFullyForce(wch, vec);
	}

	/** Close for writing */
	@Override
	public synchronized void closeWriting() throws IOException {
		if (wch != null) {
			wch.close();
			wch = null;
		}
	}

	/** Close for reading and writing */
	@Override
	public synchronized void close() throws IOException {
		closeWriting();
		if (rch != null) {
			rch.close();
		}
	}

	@Override
	public void doForEachKey(KeyIterator iter) throws Exception {
		byte[] header = new byte[HEADER_SIZE];
		ByteBuffer h = ByteBuffer.wrap(header);
		long pos = 0;
		long read = 0;
		do {
			h.clear();
			read = IO.read(rch, h, pos);
			pos += HEADER_SIZE;
			if (read <= 0) {
				// 读完了
				break;
			}
			if (read != HEADER_SIZE) {
				throw new IterException("read bytes:" + read + " HEADER_SIZE:" + HEADER_SIZE);
			}

			h.rewind();

			int checkCrc32 = h.getInt();
			long version = h.getLong();
			long expire = h.getLong();
			long position = h.getLong();
			int size = h.getInt();
			int crc32 = h.getInt();
			short keySize = h.getShort();

			byte[] k = new byte[keySize];
			ByteBuffer key = ByteBuffer.wrap(k);

			read = IO.read(rch, key, pos);
			pos += keySize;
			if (read <= 0) {
				// 读完了
				break;
			}
			if (read != keySize) {
				throw new IterException("read bytes:" + read + " keySize:" + keySize);
			}
			h.rewind();
			key.rewind();
			String keyString = Charset.forName(CHARSET).decode(key.asReadOnlyBuffer()).toString();

			// 扫描hint时需要检查crc32
			CRC32 crc = new CRC32();
			crc.update(h.array(), 4, HEADER_SIZE - 4);
			crc.update(key.array());
			if (checkCrc32 != crc.getValue()) {
				log.error("crc32 not match. key:{}, fileId:{}", keyString, this.fileId);
				continue;
			}

			iter.each(keyString, version, expire, position, size, crc32);
		} while (read > 0);
	}

}

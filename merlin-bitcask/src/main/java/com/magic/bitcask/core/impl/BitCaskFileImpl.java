package com.magic.bitcask.core.impl;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.magic.bitcask.core.BitCaskFile;
import com.magic.bitcask.core.KeyIterator;
import com.magic.bitcask.core.RecordIterator;
import com.magic.bitcask.entity.BitCaskKey;
import com.magic.bitcask.entity.BitCaskRecord;
import com.magic.bitcask.entity.BitCaskValue;
import com.magic.bitcask.exception.IterException;
import com.magic.util.CRC32;
import com.magic.util.IO;
import com.magic.util.Util;

public class BitCaskFileImpl implements BitCaskFile {

	private Logger log = LoggerFactory.getLogger(getClass());

	private static final String CHARSET = "UTF-8";

	// 4+8+8+2+4
	private static final int HEADER_SIZE = 26;

	private FileChannel wch = null;
	private FileChannel rch;

	private AtomicLong writeOffset;
	private final File file;
	private final int fileId;

	public BitCaskFileImpl(File file, FileChannel rch) {
		this.fileId = Util.getFileId(file);
		this.file = file;
		this.rch = rch;
	}

	public BitCaskFileImpl(File file, FileChannel wch, FileChannel rch) throws IOException {
		this.fileId = Util.getFileId(file);
		this.file = file;
		this.wch = wch;
		this.rch = rch;
		this.writeOffset = new AtomicLong(rch.size());
	}

	public BitCaskFileImpl() {
		this.file = null;
		this.fileId = -1;
	}

	@Override
	public File getFile() {
		return file;
	}

	@Override
	public BitCaskValue read(long offset, int length) throws IOException {

		byte[] header = new byte[HEADER_SIZE];

		ByteBuffer h = ByteBuffer.wrap(header);
		long read = IO.read(rch, h, offset);
		if (read != HEADER_SIZE) {
			throw new IOException("cannot read header @ 0x" + Long.toHexString(offset) + " read bytes:" + read
					+ " HEADER_SIZE:" + HEADER_SIZE);
		}

		int crc32 = h.getInt(0);
		long version = h.getLong(4);
		long expire = h.getLong(12);
		short keyLen = (short) h.getChar(20);
		int valLen = h.getInt(22);

		int keyValSize = keyLen + valLen;

		if (length != (HEADER_SIZE + keyValSize)) {
			throw new IOException("bad entry size. length:" + length + " HEADER_SIZE:" + HEADER_SIZE + " keyLen:"
					+ keyLen + " valLen:" + valLen);
		}

		byte[] kv = new byte[keyValSize];
		ByteBuffer keyVal = ByteBuffer.wrap(kv);

		long kvPos = offset + HEADER_SIZE;
		read = IO.read(rch, keyVal, kvPos);
		if (read != keyValSize) {
			throw new IOException("cannot read key+value @ 0x" + Long.toHexString(offset));
		}

		CRC32 crc = new CRC32();
		crc.update(header, 4, HEADER_SIZE - 4);
		crc.update(kv);

		if (crc.getValue() != crc32) {
			throw new IOException(
					"Mismatching CRC code. crc32 in file: " + crc32 + " crc32 calculate: " + crc.getValue());
		}

		BitCaskValue result = new BitCaskValue();
		result.setVersion(version);
		result.setExpire(expire);
		result.setKey(new String(kv, 0, keyLen));
		result.setValue(new String(kv, keyLen, valLen));
		return result;
	}

	@Override
	public BitCaskKey write(String key, String value, long version, long expire) throws IOException {
		BitCaskRecord record = fileEntry(key, value, version, expire);

		int entrySize = HEADER_SIZE + record.getKeySize() + record.getValueSize();
		long entryPos = writeOffset.getAndAdd(entrySize);
		IO.writeFullyForce(wch, record.getBuffers());

		return new BitCaskKey(fileId, version, expire, entryPos, entrySize, record.getCrc());
	}

	@Override
	public void writeByteBufferAndUpdateKey(BitCaskKey bck, ByteBuffer[] bb) throws IOException {
		long entrySize = 0;
		for (ByteBuffer b : bb) {
			entrySize += b.capacity();
		}
		IO.writeFullyForce(wch, bb);
		long entryPos = writeOffset.getAndAdd(entrySize);

		bck.setFileId(fileId);
		bck.setPosition(entryPos);
	}

	private BitCaskRecord fileEntry(String key, String value, long version, long expire)
			throws UnsupportedEncodingException {
		byte[] header = new byte[HEADER_SIZE];
		ByteBuffer h = ByteBuffer.wrap(header);

		byte[] keyBytes = key.getBytes(CHARSET);
		ByteBuffer k = ByteBuffer.wrap(keyBytes).asReadOnlyBuffer();
		byte[] valueBytes = value.getBytes(CHARSET);
		ByteBuffer v = ByteBuffer.wrap(valueBytes).asReadOnlyBuffer();

		h.putLong(4, version);
		h.putLong(12, expire);
		h.putShort(20, (short) keyBytes.length);
		h.putInt(22, valueBytes.length);

		CRC32 crc = new CRC32();
		crc.update(header, 4, HEADER_SIZE - 4);
		crc.update(key);
		crc.update(value);
		int crcValue = crc.getValue();

		h.putInt(0, crcValue);

		ByteBuffer[] vec = new ByteBuffer[] { h, k, v };

		BitCaskRecord result = new BitCaskRecord(crcValue, version, expire, (short) keyBytes.length, valueBytes.length,
				key, value, vec);
		return result;
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
	public boolean needCreateNewFile(String key, String value, long maxFileSize) {
		return needCreateNewFile(key.getBytes().length, value.getBytes().length, maxFileSize);
	}

	@Override
	public boolean needCreateNewFile(int keyLength, int valueLength, long maxFileSize) {
		int size = HEADER_SIZE + keyLength + valueLength;
		if (writeOffset.get() + size >= maxFileSize) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void doForEachKey(KeyIterator iter) throws Exception {
		byte[] header = new byte[HEADER_SIZE];
		ByteBuffer h = ByteBuffer.wrap(header);
		long pos = 0;
		long read = 0;
		do {
			h.rewind();
			read = IO.read(rch, h, pos);
			pos += HEADER_SIZE;
			if (read == 0) {
				break;
			}
			if (read != HEADER_SIZE) {
				throw new IterException("read bytes:" + read + " HEADER_SIZE:" + HEADER_SIZE);
			}

			h.rewind();
			int crc32 = h.getInt();
			long version = h.getLong();
			long expire = h.getLong();
			short keyLen = (short) h.getChar();
			int valLen = h.getInt();

			byte[] k = new byte[keyLen];
			ByteBuffer key = ByteBuffer.wrap(k);

			read = IO.read(rch, key, pos);
			pos += keyLen;
			// 将读取指针指向下一条记录
			pos += valLen;
			if (read != keyLen) {
				throw new IterException("read bytes:" + read + " keyLen:" + keyLen);
			}

			int entrySize = HEADER_SIZE + keyLen + valLen;

			key.rewind();
			String keyString = Charset.forName(CHARSET).decode(key.asReadOnlyBuffer()).toString();

			iter.each(keyString, version, expire, pos - entrySize, entrySize, crc32);
		} while (read > 0);
	}

	@Override
	public void doForEachRecord(RecordIterator iter) throws Exception {
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
			int crc32 = h.getInt();
			h.getLong(); // version
			h.getLong(); // expire
			int keyLen = h.getChar();
			int valLen = h.getInt();

			byte[] k = new byte[keyLen];
			ByteBuffer key = ByteBuffer.wrap(k);

			read = IO.read(rch, key, pos);
			pos += keyLen;
			if (read != keyLen) {
				throw new IterException("read bytes:" + read + " keyLen:" + keyLen);
			}

			byte[] v = new byte[valLen];
			ByteBuffer value = ByteBuffer.wrap(v);
			read = IO.read(rch, value, pos);
			pos += valLen;
			if (read != valLen) {
				throw new IterException("read bytes:" + read + " valLen:" + valLen);
			}

			h.rewind();
			key.rewind();
			value.rewind();
			String keyString = Charset.forName(CHARSET).decode(key.asReadOnlyBuffer()).toString();

			// merge时需要检查crc32
			CRC32 crc = new CRC32();
			crc.update(h.array(), 4, HEADER_SIZE - 4);
			crc.update(key.array());
			crc.update(value.array());
			if (crc32 != crc.getValue()) {
				log.error("crc32 not match. key: " + keyString + " crc32 in file: " + crc32 + " crc32 calculate: "
						+ crc.getValue());
				continue;
			}

			iter.each(keyString, crc32, new ByteBuffer[] { h, key, value });
		} while (read > 0);
	}

	@Override
	public boolean isWriteable() {
		return wch != null;
	}

}

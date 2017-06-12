package com.magic.bitcask.core.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileAlreadyExistsException;

import com.magic.bitcask.core.BitCaskFile;
import com.magic.bitcask.enums.Stale;
import com.magic.bitcask.enums.Type;
import com.magic.util.OS;

public class BitCaskLock {

	private RandomAccessFile file;

	private boolean isWriteLock;

	private File filename;

	private BitCaskLock(RandomAccessFile file, File filename, boolean isWriteLock) {
		this.file = file;
		this.filename = filename;
		this.isWriteLock = isWriteLock;
	}

	public String getLockFileAbsolutePath() {
		return filename.getAbsolutePath();
	}

	public static File readActivefile(Type type, File dirname) {
		File lockFilename = lockFilename(type, dirname);
		try {
			BitCaskLock lock = lockAcquire(lockFilename, false);

			try {
				String contents = lock.readLockData();

				int idx = contents.indexOf(' ');
				if (idx != -1) {
					String rest = contents.substring(idx + 1);

					int end = rest.indexOf('\n');
					if (end != -1) {
						String path = rest.substring(0, end);
						return new File(path);
					}
				}

			} finally {
				lock.release();
			}

		} catch (Exception e) {
		}

		return null;

	}

	public void writeActivefile(Type type, File dirname) throws IOException {
		writeActivefile(lockFilename(type, dirname));
	}

	public void writeActivefile(BitCaskFile file) throws IOException {
		writeActivefile(file.getFile());
	}

	private void writeActivefile(File activeFilename) throws IOException {
		String lockContents = Integer.toString(OS.getpid()) + " " + activeFilename.getPath() + "\n";
		writeData(lockContents);
	}

	public static BitCaskLock acquire(Type type, File dirname) throws IOException {
		File lockFilename = lockFilename(type, dirname);
		try {
			BitCaskLock lock = lockAcquire(lockFilename, true);

			String lockContents = Integer.toString(OS.getpid()) + " \n";
			lock.writeData(lockContents);

			return lock;

		} catch (FileAlreadyExistsException e) {
			deleteStaleLock(lockFilename);
		}
		return null;
	}

	private void writeData(String data) throws IOException {
		FileChannel ch = file.getChannel();
		if (isWriteLock) {
			ch.truncate(0);
			ch.write(ByteBuffer.wrap(data.getBytes()).asReadOnlyBuffer(), 0);

			return;
		}

		throw new IOException("file not writable");
	}

	public static Stale deleteStaleLock(Type type, File dirname) throws IOException {
		return deleteStaleLock(lockFilename(type, dirname));
	}

	private static Stale deleteStaleLock(File lockFilename) throws IOException {

		BitCaskLock l = null;

		try {
			l = lockAcquire(lockFilename, false);

		} catch (FileNotFoundException e) {
			return Stale.OK;
		} catch (IOException e) {
			return Stale.NOT_STALE;
		}

		try {

			int pid = l.readLockDataPid();

			if (OS.pidExists(pid)) {
				return Stale.NOT_STALE;
			} else {
				lockFilename.delete();
				return Stale.OK;
			}

		} catch (IOException e) {
			return Stale.NOT_STALE;
		} finally {
			l.release();
		}

	}

	private int readLockDataPid() throws IOException {
		String data = readLockData();

		int idx = data.indexOf(' ');
		if (idx != -1) {
			return Integer.parseInt(data.substring(0, idx));
		}

		throw new IOException();
	}

	private String readLockData() throws IOException {
		this.file.seek(0);
		int len = (int) file.length();
		byte[] data = new byte[len];
		file.read(data);
		return new String(data);
	}

	public void release() throws IOException {
		if (file != null) {

			if (isWriteLock) {
				file.close();
				filename.delete();
			}
		}

		file = null;
	}

	private static BitCaskLock lockAcquire(File lockFilename, boolean isWriteLock) throws IOException {

		if (isWriteLock) {
			if (lockFilename.createNewFile() == false) {
				// file already exists, so we fail!

				throw new FileAlreadyExistsException(lockFilename.getPath());
			}
		}

		RandomAccessFile f = new RandomAccessFile(lockFilename, isWriteLock ? "rws" : "r");

		return new BitCaskLock(f, lockFilename, isWriteLock);
	}

	private static File lockFilename(Type type, File dirname) {
		return new File(dirname, "bitcask." + type.getTypeName() + ".lock");
	}

	public void close() throws IOException {
		file.close();
	}

}

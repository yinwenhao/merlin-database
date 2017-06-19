package com.magic.bitcask.core.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.magic.bitcask.core.BitCaskFile;
import com.magic.bitcask.core.BitCaskHint;
import com.magic.bitcask.core.BitCaskKeydir;
import com.magic.bitcask.core.BitCaskServer;
import com.magic.bitcask.core.RecordIterator;
import com.magic.bitcask.core.factory.BitCaskFileFactory;
import com.magic.bitcask.core.factory.BitCaskHintFactory;
import com.magic.bitcask.entity.BitCaskKey;
import com.magic.bitcask.entity.BitCaskValue;
import com.magic.bitcask.enums.Type;
import com.magic.bitcask.exception.ReadOnlyException;
import com.magic.bitcask.options.BitCaskOptions;
import com.magic.constants.Constants;
import com.magic.exception.BaseException;
import com.magic.synchronize.merkletree.MerkleTreeNode;
import com.magic.timer.ScheduledExecutorServiceTimer;
import com.magic.util.Util;

public class BitCaskImpl implements BitCaskServer, Runnable {

	private Logger log = LoggerFactory.getLogger(getClass());

	private static final String TOMBSTONE = "bitcask_tombstone";

	private File dirname;

	private BitCaskFile writeFile;

	private BitCaskLock writeLock;

	private Map<File, BitCaskFile> readFiles = new HashMap<File, BitCaskFile>();

	private long maxFileSize;

	private BitCaskKeydir keyManager;

	private BitCaskFileFactory bitCaskFileFactory;

	private BitCaskHintFactory bitCaskHintFactory;

	private MerkleTreeNode merkleTreeRoot;

	private BitCaskOptions bitCaskOptions;

	private ReadOnlyException roe = new ReadOnlyException();

	public void freshInit(File file) throws Exception {
		if (writeFile == null) {
			throw new IOException("no write file");
		}
		BitCaskLock wl = BitCaskLock.acquire(Type.WRITE, dirname);
		if (wl == null) {
			// 没有拿到锁
			throw new IOException("failed to get write lock.");
		}
		BitCaskFile nwf;
		if (file == null || this.checkHintFileExist(file) != null) {
			// 没有存储文件，或者存在对应的hint文件，新生成一个存储文件
			nwf = bitCaskFileFactory.createBitCaskFile(dirname);
		} else {
			nwf = bitCaskFileFactory.openBitCaskFileForWrite(file);
		}
		readFiles.put(nwf.getFile(), nwf);
		this.writeLock = wl;
		this.writeFile = nwf;
	}

	// 这里需要处理并发问题，或者保证相同的key都在同一个(或保证时序的不同线程)线程中
	private void doSetWithExpire(String key, String value, long version, long expire)
			throws IOException, BaseException {
		if (writeFile == null) {
			throw new IOException("no write file");
		}
		if (log.isDebugEnabled()) {
			Thread current = Thread.currentThread();
			log.debug("thread name:{} hashcode:{}", current.getName(), current.hashCode());
		}
		BitCaskKey bck = keyManager.get(key);
		if (bck != null && bck.getVersion() >= version) {
			// 目前版本号较新，不需要更新
			return;
		}
		log.debug("keyManager pass" + key);
		if (writeFile.needCreateNewFile(key, value, maxFileSize)) {
			synchronized (this) {
				if (writeFile.needCreateNewFile(key, value, maxFileSize)) {
					writeFile = createNewWriteFileAndDelayToCloseOldWriteFile(writeFile);
				}
			}
		}
		log.debug("before write key:{} value:{} version:{} expire:{}", key, value, version, expire);
		BitCaskKey entry = writeFile.write(key, value, version, expire);
		log.debug("after write key:{} value:{} version:{} expire:{}", key, value, version, expire);
		keyManager.put(key, entry);
		log.debug("after keyManager.put key:" + key);
		merkleTreeRoot.addOrUpdateLeaf(key, entry.getCrc32());
		log.debug("after merkleTreeRoot.addOrUpdateLeaf key:" + key);
	}

	@Override
	public void innerSetWithExpire(String key, String value, long version, long expire)
			throws IOException, BaseException {
		doSetWithExpire(key, value, version, expire);
	}

	@Override
	public void set(String key, String value, long version) throws IOException, BaseException {
		setWithExpire(key, value, version, Constants.EXPIRE_TIME_DEFAULT);
	}

	@Override
	public void setWithExpire(String key, String value, long version, long expire) throws IOException, BaseException {
		if (!bitCaskOptions.readWrite) {
			throw roe;
		}
		doSetWithExpire(key, value, version, expire);
	}

	private BitCaskFile createNewWriteFileAndDelayToCloseOldWriteFile(final BitCaskFile oldWriteFile)
			throws IOException {
		ScheduledExecutorServiceTimer.getInstance().schedule(new Runnable() {
			@Override
			public void run() {
				// 延时关闭老的文件
				try {
					oldWriteFile.closeWriting();
				} catch (IOException e) {
					log.error("oldWriteFile.closeWriting() error", e);
					ScheduledExecutorServiceTimer.getInstance().schedule(this, bitCaskOptions.closeWriteDelay,
							TimeUnit.MILLISECONDS);
				}
			}
		}, bitCaskOptions.closeWriteDelay, TimeUnit.MILLISECONDS);
		BitCaskFile nwf = bitCaskFileFactory.createBitCaskFile(dirname);
		// 将新生成的存储文件放入可读文件列表
		readFiles.put(nwf.getFile(), nwf);
		return nwf;

	}

	@Override
	public BitCaskValue getRealValue(String key) throws IOException {
		return doGet(key, 2, false);
	}

	@Override
	public BitCaskValue get(String key) throws IOException {
		return doGet(key, bitCaskOptions.bitcaskGetMaxRetries, true);
	}

	private BitCaskValue doGet(String key, int tryNum, boolean isRead) throws IOException {
		if (tryNum <= 0) {
			throw new IOException();
		}
		BitCaskKey entry = keyManager.get(key);
		if (entry == null) {
			return new BitCaskValue(key);
		}

		if (isRead) {
			if (entry.getExpire() > 0 && System.currentTimeMillis() >= entry.getExpire()) {
				// 数据过期了
				keyManager.checkExpire(key);
				return new BitCaskValue(key);
			}
		}

		BitCaskFile file_state = getFilestate(entry.getFileId());
		/** merging deleted file between keydir.get and here */
		if (file_state == null) {
			Thread.yield();
			return doGet(key, tryNum - 1, isRead);
		}

		BitCaskValue bcv = file_state.read(entry.getPosition(), entry.getSize());

		if (isRead) {
			if (TOMBSTONE.equals(bcv.getValue())) {
				bcv.setValue(null);
			}
		}
		return bcv;
	}

	@Override
	public void delete(String key, long version) throws IOException, BaseException {
		setWithExpire(key, TOMBSTONE, version, Constants.EXPIRE_TIME_DEFAULT);
	}

	public void addReadFile(BitCaskFile readFile) {
		readFiles.put(readFile.getFile(), readFile);
	}

	private BitCaskFile getFilestate(int fileId) throws IOException {
		File fname = Util.makeDataFilename(dirname, fileId);
		BitCaskFile f = readFiles.get(fname);
		if (f != null) {
			return f;
		}

		f = bitCaskFileFactory.openBitCaskFile(fname);
		readFiles.put(fname, f);

		return f;
	}

	/**
	 * 根据存储文件获得hint文件
	 * 
	 * @param dataFile
	 * @return 如果不存在hint，则返回null
	 * @throws IOException
	 */
	public File checkHintFileExist(File dataFile) throws IOException {
		int fileId = Util.getFileId(dataFile);
		File fname = Util.makeHintFilename(dirname, fileId);
		if (!fname.exists()) {
			return null;
		} else {
			return fname;
		}
	}

	public File[] readableFiles() {
		return listDataFiles();
	}

	private static Pattern DATA_FILE = Pattern.compile("[0-9]+.bitcask.data");

	private File[] listDataFiles() {
		File[] files = dirname.listFiles(new FileFilter() {
			@Override
			public boolean accept(File f) {
				return DATA_FILE.matcher(f.getName()).matches();
			}
		});

		Arrays.sort(files, 0, files.length, REVERSE_DATA_FILE_COMPARATOR);

		return files;
	}

	private static final Comparator<? super File> REVERSE_DATA_FILE_COMPARATOR = new Comparator<File>() {

		@Override
		public int compare(File file0, File file1) {
			int i0 = Util.getFileId(file0);
			int i1 = Util.getFileId(file1);

			if (i0 < i1) {
				return 1;
			} else if (i0 == i1) {
				return 0;
			} else {
				return -1;
			}
		}
	};

	public File getDirname() {
		return dirname;
	}

	public void setDirname(File dirname) {
		this.dirname = dirname;
	}

	public BitCaskFile getWriteFile() {
		return writeFile;
	}

	public void setWriteFile(BitCaskFile writeFile) {
		this.writeFile = writeFile;
	}

	public BitCaskLock getWriteLock() {
		return writeLock;
	}

	public void setWriteLock(BitCaskLock writeLock) {
		this.writeLock = writeLock;
	}

	public Map<File, BitCaskFile> getReadFiles() {
		return readFiles;
	}

	public void setReadFiles(Map<File, BitCaskFile> readFiles) {
		this.readFiles = readFiles;
	}

	public long getMaxFileSize() {
		return maxFileSize;
	}

	public void setMaxFileSize(long maxFileSize) {
		this.maxFileSize = maxFileSize;
	}

	public BitCaskKeydir getKeyManager() {
		return keyManager;
	}

	public void setKeyManager(BitCaskKeydir keyManager) {
		this.keyManager = keyManager;
	}

	public BitCaskFileFactory getBitCaskFileFactory() {
		return bitCaskFileFactory;
	}

	public void setBitCaskFileFactory(BitCaskFileFactory bitCaskFileFactory) {
		this.bitCaskFileFactory = bitCaskFileFactory;
	}

	public BitCaskHintFactory getBitCaskHintFactory() {
		return bitCaskHintFactory;
	}

	public void setBitCaskHintFactory(BitCaskHintFactory bitCaskHintFactory) {
		this.bitCaskHintFactory = bitCaskHintFactory;
	}

	public BitCaskOptions getOptions() {
		return bitCaskOptions;
	}

	public void setOptions(BitCaskOptions bitCaskOptions) {
		this.bitCaskOptions = bitCaskOptions;
	}

	@Override
	public MerkleTreeNode getMerkleTreeRoot() {
		return merkleTreeRoot;
	}

	public void setMerkleTreeRoot(MerkleTreeNode merkleTreeRoot) {
		this.merkleTreeRoot = merkleTreeRoot;
	}

	@Override
	public void close() throws IOException {
		writeFile.close();
		writeLock.release();
	}

	@Override
	public void mergeAll() throws Exception {
		if (readFiles.size() > bitCaskOptions.fileNumToMerge) {
			// 存在只读存储文件，做合并清理
			log.info("merge start...");
			BitCaskLock wl = BitCaskLock.acquire(Type.MERGE, dirname);
			if (wl == null) {
				// 没有拿到锁
				throw new IOException("failed to get merge lock.");
			}
			log.info("merge get lock file: " + wl.getLockFileAbsolutePath());
			// 创建新的存储文件，用来写入合并后的数据
			BitCaskFile nwf = bitCaskFileFactory.createBitCaskFile(dirname);
			log.info("merge create resource file: " + nwf.getFile().getAbsolutePath());
			// hint文件的生成
			BitCaskHint bch = bitCaskHintFactory.createBitCaskHint(dirname, Util.getFileId(nwf.getFile()));
			log.info("merge create hint file: " + bch.getFile().getAbsolutePath());
			// 复制一份可读存储文件列表
			List<BitCaskFile> reads = new ArrayList<BitCaskFile>(readFiles.values());
			// 把新的存储文件放入可读文件列表
			readFiles.put(nwf.getFile(), nwf);
			for (BitCaskFile bcf : reads) {
				if (bcf == writeFile || bcf.isWriteable()) {
					continue;
				}
				bcf.doForEachRecord(new RecordIterator() {
					@Override
					public void each(String key, int crc32, ByteBuffer[] bb) throws Exception {
						BitCaskKey bck = keyManager.get(key);
						if (bck == null) {
							// 这是理论上不可能出现的情况
							log.error("this is impossible.");
							return;
						}
						if (crc32 == bck.getCrc32()) {
							// 是这个key的最新值，写到新存储文件中，并更新索引
							BitCaskFile writeFile = nwf;
							BitCaskHint writeHint = bch;
							if (writeFile.needCreateNewFile(bb[1].capacity(), bb[2].capacity(),
									bitCaskOptions.maxFileSize)) {
								// 延迟关闭writeFile的写通道
								writeFile = createNewWriteFileAndDelayToCloseOldWriteFile(writeFile);
								// 直接关闭writeHint
								writeHint.close();
								writeHint = bitCaskHintFactory.createBitCaskHint(dirname,
										Util.getFileId(writeFile.getFile()));
							}
							writeFile.writeByteBufferAndUpdateKey(bck, bb);
							// hint文件的写入
							writeHint.write(key, bck);
						}
					}
				});
				File hintFile = checkHintFileExist(bcf.getFile());
				if (hintFile != null) {
					// 存在老的存储文件对应的hint文件，删除这个hint文件
					hintFile.delete();
					log.info("merge delete old hint file: " + hintFile.getAbsolutePath());
				}
				// 关闭并删除老的存储文件
				readFiles.remove(bcf.getFile());
				bcf.close();
				bcf.getFile().delete();
				log.info("merge delete old resource file: " + bcf.getFile().getAbsolutePath());
			}
			wl.release();
			log.info("merge finish.");
		}
	}

	@Override
	public void scheduleMerge() throws Exception {
		// 每天低峰的时候执行，比如半夜3点
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, bitCaskOptions.hourToMerge);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		if (System.currentTimeMillis() > cal.getTimeInMillis()) {
			cal.add(Calendar.DAY_OF_YEAR, 1);
		}
		ScheduledExecutorServiceTimer.getInstance().scheduleAtFixedRate(this, cal.getTime(),
				bitCaskOptions.periodDayToMerge, TimeUnit.DAYS);

		// 这里是为了方便测试改的代码，记得改回去！！！！！！！！！
		// ScheduledExecutorServiceTimer.getInstance().scheduleAtFixedRate(this,
		// new Date(System.currentTimeMillis() + 10000),
		// bitCaskOptions.periodDayToMerge, TimeUnit.DAYS);
	}

	@Override
	public void run() {
		try {
			mergeAll();
		} catch (Exception e) {
			log.error("mergeAll error", e);
		} catch (Throwable t) {
			log.error("mergeAll error", t);
		}
	}

}

package com.magic.bitcask.core.factory.impl;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.magic.bitcask.core.BitCaskFile;
import com.magic.bitcask.core.BitCaskHint;
import com.magic.bitcask.core.BitCaskKeydir;
import com.magic.bitcask.core.BitCaskServer;
import com.magic.bitcask.core.KeyIterator;
import com.magic.bitcask.core.factory.BitCaskFactory;
import com.magic.bitcask.core.factory.BitCaskFileFactory;
import com.magic.bitcask.core.factory.BitCaskHintFactory;
import com.magic.bitcask.core.factory.BitCaskKeyManagerFactory;
import com.magic.bitcask.core.impl.BitCaskFileImpl;
import com.magic.bitcask.core.impl.BitCaskImpl;
import com.magic.bitcask.core.impl.BitCaskLock;
import com.magic.bitcask.entity.BitCaskKey;
import com.magic.bitcask.enums.Type;
import com.magic.bitcask.options.BitCaskOptions;
import com.magic.synchronize.merkletree.MerkleTreeNode;
import com.magic.util.Util;

public class BitCaskFactoryImpl implements BitCaskFactory {

	private Logger log = LoggerFactory.getLogger(getClass());

	public static final BitCaskFileImpl FRESH_FILE = new BitCaskFileImpl();

	private BitCaskKeyManagerFactory keyManagerFactory = new BitCaskKeyManagerFactoryImpl();

	private BitCaskFileFactory bitCaskFileFactory = new BitCaskFileFactoryImpl();

	private BitCaskHintFactory bitCaskHintFactory = new BitCaskHintFactoryImpl();

	private static final String DATABASE_DIR = "bitcaskdb";

	@Override
	public BitCaskServer createBitCask(File dirname, BitCaskOptions opts) throws Exception {
		BitCaskImpl result = new BitCaskImpl();

		File dbDir = new File(dirname, DATABASE_DIR);
		Util.ensuredir(dbDir);
		log.info("bitcask using dir: " + dbDir.getAbsolutePath());

		if (opts.readWrite) {
			BitCaskLock.deleteStaleLock(Type.WRITE, dbDir);
			result.setWriteFile(FRESH_FILE);
		}

		result.setDirname(dbDir);

		BitCaskKeydir keyManager = keyManagerFactory.createBitCaskKeyManager(dbDir, opts.openTimeoutSecs);
		result.setKeyManager(keyManager);
		File[] files = null;
		if (!keyManager.isReady()) {
			files = result.readableFiles();
			scanKeyFiles(result, files, keyManager);
			keyManager.markReady();
		}

		result.setMaxFileSize(opts.maxFileSize);
		result.setDirname(dbDir);
		result.setOptions(opts);
		result.setBitCaskFileFactory(bitCaskFileFactory);
		result.setBitCaskHintFactory(bitCaskHintFactory);

		result.freshInit(files == null || files.length <= 0 ? null : files[0]);
		return result;
	}

	private void scanKeyFiles(BitCaskImpl result, File[] files, final BitCaskKeydir keydir) throws Exception {
		MerkleTreeNode root = new MerkleTreeNode(0, 0);
		for (File f : files) {
			BitCaskFile bcFile = bitCaskFileFactory.openBitCaskFile(f);
			result.addReadFile(bcFile);

			KeyIterator ki = new KeyIterator() {

				@Override
				public void each(String key, long version, long expire, long position, int size, int crc32)
						throws Exception {
					BitCaskKey bck = new BitCaskKey(Util.getFileId(f), version, expire, position, size, crc32);
					keydir.put(key, bck);
					root.addOrUpdateLeafWithoutHash(key, crc32);
				}
			};

			File hintFile = result.checkHintFileExist(f);
			if (hintFile != null) {
				// 存在hint文件
				BitCaskHint bch = bitCaskHintFactory.openBitCaskHint(hintFile);
				log.info("data file: " + f.getAbsolutePath() + " has hint file, scan hint file: "
						+ hintFile.getAbsolutePath() + "instead.");
				bch.doForEachKey(ki);
				bch.close();
			} else {
				// 不存在hint文件，扫描存储文件
				log.info("scan data file: " + f.getAbsolutePath());
				bcFile.doForEachKey(ki);
			}
		}
		root.fillHashForMerkleTree();
		result.setMerkleTreeRoot(root);
	}

}

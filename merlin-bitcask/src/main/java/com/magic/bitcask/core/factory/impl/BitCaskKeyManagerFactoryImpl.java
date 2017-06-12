package com.magic.bitcask.core.factory.impl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.magic.bitcask.core.BitCaskKeydir;
import com.magic.bitcask.core.factory.BitCaskKeyManagerFactory;
import com.magic.bitcask.core.impl.BitCaskKeydirImpl;

public class BitCaskKeyManagerFactoryImpl implements BitCaskKeyManagerFactory {

	public static Map<File, BitCaskKeydir> managerMap = new HashMap<File, BitCaskKeydir>();
	public static Lock keydir_lock = new ReentrantLock();

	@Override
	public BitCaskKeydir createBitCaskKeyManager(File dirname, int openTimeoutSecs) throws IOException {
		File abs_name = dirname.getAbsoluteFile();
		BitCaskKeydir result;
		keydir_lock.lock();
		try {

			result = managerMap.get(abs_name);
			if (result == null) {
				result = new BitCaskKeydirImpl();
				managerMap.put(abs_name, result);
				return result;
			}

		} finally {
			keydir_lock.unlock();
		}

		if (result.waitForReady(openTimeoutSecs)) {
			return result;
		} else {
			throw new IOException("timeout while waiting for keydir");
		}
	}

}

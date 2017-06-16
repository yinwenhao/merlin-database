package com.magic.bitcask.core.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.magic.bitcask.core.BitCaskKeydir;
import com.magic.bitcask.entity.BitCaskKey;

public class BitCaskKeydirImpl implements BitCaskKeydir {

	Map<String, BitCaskKey> keyDir = new HashMap<String, BitCaskKey>();
	ReadWriteLock rwl = new ReentrantReadWriteLock();
	private boolean isReady;

	@Override
	public synchronized boolean isReady() {
		return isReady;
	}

	@Override
	public synchronized void markReady() {
		isReady = true;
		this.notifyAll();
	}

	@Override
	public synchronized boolean waitForReady(int timeoutSecs) {
		long now = System.currentTimeMillis();
		long abs_timeout = now + (timeoutSecs * 1000);
		while (!isReady && now < abs_timeout) {
			try {
				wait();
			} catch (InterruptedException e) {
				// ignore
			}
			now = System.currentTimeMillis();
		}
		return isReady;
	}

	@Override
	public boolean put(String key, BitCaskKey record) {
		Lock writeLock = rwl.writeLock();
		writeLock.lock();
		try {
			BitCaskKey old = keyDir.get(key);
			if (old == null) {
				keyDir.put(key, record);
				return true;
			} else if (record.isNewerThan(old)) {
				keyDir.put(key, record);
				return true;
			} else {
				return false;
			}
		} finally {
			writeLock.unlock();
		}
	}

	@Override
	public BitCaskKey get(String key) {
		Lock readLock = rwl.readLock();
		readLock.lock();
		try {
			return keyDir.get(key);
		} finally {
			readLock.unlock();
		}
	}

	@Override
	public void checkExpire(String key) {
		Lock writeLock = rwl.writeLock();
		writeLock.lock();
		try {
			BitCaskKey old = keyDir.get(key);
			if (old.getExpire() > 0 && System.currentTimeMillis() >= old.getExpire()) {
				// 数据过期了
				keyDir.remove(key);
			}
		} finally {
			writeLock.unlock();
		}
	}

}

package com.magic.executor;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecutorImpl implements Executor {

	private final Logger log = LoggerFactory.getLogger(getClass());

	/**
	 * 等待线程池关闭的时间（秒）
	 */
	private static final long AWAIT_SECONDS = 15;

	// 创建一个可重用固定线程数的线程池
	private ThreadPoolExecutor pool;

	private int poolSize = 8;

	public ExecutorImpl() {
	}

	public ExecutorImpl(int poolSize) {
		this.poolSize = poolSize;
	}

	@Override
	public void execute(Runnable task) {
		pool.execute(task);
	}

	@Override
	public void destroy() throws InterruptedException {
		pool.shutdown();
		if (!pool.awaitTermination(AWAIT_SECONDS, TimeUnit.SECONDS)) {
			log.warn("pool: close threads... ({}) take too long time: {}s.", new Date(), AWAIT_SECONDS);
		}
	}

	@Override
	public void init() {
		pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(poolSize);
	}

	public int getPoolSize() {
		return poolSize;
	}

	public void setPoolSize(int poolSize) {
		this.poolSize = poolSize;
	}

}

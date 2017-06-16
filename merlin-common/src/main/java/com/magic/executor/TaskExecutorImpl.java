package com.magic.executor;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.magic.executor.task.Task;

public class TaskExecutorImpl implements TaskExecutor {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private static final int defaultPoolSize = 8;

	/**
	 * 等待线程池关闭的时间（秒）
	 */
	private static final long AWAIT_SECONDS = 15;

	private ExecutorService[] singleThreadPools;

	private int poolSize;

	private Random ran;

	public TaskExecutorImpl() {
		this(defaultPoolSize);
	}

	public TaskExecutorImpl(int poolSize) {
		this.poolSize = poolSize;
		this.ran = new Random();
	}

	@Override
	public void execute(Runnable task) {
		log.debug("execute Runnable");
		singleThreadPools[this.ran.nextInt(this.poolSize)].execute(task);
	}

	@Override
	public void shutdown() throws InterruptedException {
		for (ExecutorService pool : singleThreadPools) {
			pool.shutdown();
			if (!pool.awaitTermination(AWAIT_SECONDS, TimeUnit.SECONDS)) {
				log.warn("pool: close threads... ({}) take too long time: {}s.", new Date(), AWAIT_SECONDS);
			}
		}
	}

	@Override
	public void init() {
		// 创建一组单线程的线程池
		this.singleThreadPools = new ExecutorService[this.poolSize];
		for (int i = 0; i < this.poolSize; i++) {
			this.singleThreadPools[i] = (ExecutorService) Executors.newSingleThreadExecutor();
		}
	}

	public int getPoolSize() {
		return poolSize;
	}

	public void setPoolSize(int poolSize) {
		this.poolSize = poolSize;
	}

	@Override
	public void execute(Task task) {
		int threadIndex = task.getThreadIndex() % this.poolSize;
		if (threadIndex < 0) {
			threadIndex = -threadIndex;
		}
		log.debug("execute Task getThreadIndex:{} threadIndex:{}", task.getThreadIndex(), threadIndex);
		singleThreadPools[threadIndex].execute(task);
	}

}

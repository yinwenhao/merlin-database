package com.magic.executor.factory;

import com.magic.executor.Executor;
import com.magic.executor.ExecutorImpl;
import com.magic.executor.TaskExecutor;
import com.magic.executor.TaskExecutorImpl;

public class ExecutorFactory {

	/**
	 * 一个固定大小的线程池，默认线程池大小：{@link com.magic.executor.ExecutorImpl#defaultPoolSize}
	 * 
	 * @return Executor
	 */
	public static Executor createRequestExecutorAndInit() {
		Executor result = new ExecutorImpl();
		result.init();
		return result;
	}

	/**
	 * 一个固定大小的线程池
	 * 
	 * @param poolSize
	 * @return Executor
	 */
	public static Executor createRequestExecutorAndInit(int poolSize) {
		Executor result = new ExecutorImpl(poolSize);
		result.init();
		return result;
	}

	/**
	 * 一个线程池，默认线程池大小：{@link com.magic.executor.TaskExecutorImpl#defaultPoolSize}
	 * <p>
	 * 会保证相同的{@link com.magic.executor.task.Task#getThreadIndex}的Task的时序
	 * 
	 * @return TaskExecutor
	 */
	public static TaskExecutor createRequestTaskExecutorAndInit() {
		TaskExecutor result = new TaskExecutorImpl();
		result.init();
		return result;
	}

	/**
	 * <p>
	 * 会保证相同的{@link com.magic.executor.task.Task#getThreadIndex}的Task的时序
	 * 
	 * @return TaskExecutor
	 */
	public static TaskExecutor createRequestTaskExecutorAndInit(int poolSize) {
		TaskExecutor result = new TaskExecutorImpl(poolSize);
		result.init();
		return result;
	}

}

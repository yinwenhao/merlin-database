package com.magic.executor.factory;

import com.magic.executor.Executor;
import com.magic.executor.ExecutorImpl;

public class ExecutorFactory {

	public static Executor createRequestExecutorAndInit() {
		Executor result = new ExecutorImpl();
		result.init();
		return result;
	}

	public static Executor createRequestExecutorAndInit(int poolSize) {
		Executor result = new ExecutorImpl(poolSize);
		result.init();
		return result;
	}

}

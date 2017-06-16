package com.magic.executor;

import com.magic.executor.task.Task;

public interface TaskExecutor extends Executor {

	/**
	 * 线程执行时会保证相同的{@link com.magic.executor.task.Task#getThreadIndex}的Task的时序
	 * 
	 * @param task
	 */
	public void execute(Task task);

}

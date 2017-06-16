package com.magic.executor;

public interface Executor {

	public void execute(Runnable task);

	public void shutdown() throws Exception;

	public void init();
}

package com.magic.executor;

public interface Executor {

	public void execute(Runnable task);

	public void destroy() throws Exception;

	public void init();
}

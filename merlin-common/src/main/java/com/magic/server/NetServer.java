package com.magic.server;

public interface NetServer {

	public void startServer() throws Exception;

	public void shutdown();

	public void sync() throws Exception;

}

package com.magic.netty.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.GenericFutureListener;

public class ExceptionListener implements GenericFutureListener<ChannelFuture> {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private ExceptionListener() {
	}

	private static class SingletonHolder {
		private static ExceptionListener instance = new ExceptionListener();
	}

	public static ExceptionListener getInstance() {
		return SingletonHolder.instance;
	}

	@Override
	public void operationComplete(ChannelFuture future) throws Exception {
		if (!future.isSuccess() && future.cause() != null) {
			log.error(future.cause().getMessage(), future.cause());
		}
	}
}

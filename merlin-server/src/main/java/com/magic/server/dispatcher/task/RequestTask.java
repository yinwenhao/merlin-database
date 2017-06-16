package com.magic.server.dispatcher.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.magic.executor.task.Task;
import com.magic.netty.request.Request;
import com.magic.netty.request.Response;
import com.magic.server.dispatcher.Dispatcher;
import com.magic.util.CRC32;

import io.netty.channel.ChannelHandlerContext;

public class RequestTask implements Task {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private static Dispatcher dispatcher;

	private ChannelHandlerContext ctx;

	private Request request;

	private int threadIndex;

	public RequestTask(Request request, ChannelHandlerContext ctx) {
		this.request = request;
		this.ctx = ctx;

		CRC32 crc32 = new CRC32();
		crc32.update(this.request.getKey());
		this.threadIndex = crc32.getValue();
		log.debug("RequestTask key:{} threadIndex:{}", this.request.getKey(), this.threadIndex);
	}

	@Override
	public void run() {
		Response response = null;
		try {
			response = dispatcher.getResult(request);
			if (response != null) {
				ctx.writeAndFlush(response);
			}
		} catch (Exception e) {
			log.error("request task error", e);
		} finally {
			if (log.isDebugEnabled()) {
				log.debug("method:{}, key:{}, value:{}. response value:{}", request.getMethod(), request.getKey(),
						request.getValue(), response == null ? "#response is null!#" : response.getValue());
			}
		}
	}

	public ChannelHandlerContext getCtx() {
		return ctx;
	}

	public void setCtx(ChannelHandlerContext ctx) {
		this.ctx = ctx;
	}

	public static void setDispatcher(Dispatcher dispatcher) {
		RequestTask.dispatcher = dispatcher;
	}

	@Override
	public int getThreadIndex() {
		return this.threadIndex;
	}

}

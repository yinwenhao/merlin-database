package com.magic.server.netty.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.magic.executor.TaskExecutor;
import com.magic.executor.factory.ExecutorFactory;
import com.magic.netty.request.Request;
import com.magic.server.dispatcher.task.RequestTask;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

@Sharable
public class TcpServerHandler extends ChannelInboundHandlerAdapter {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private TaskExecutor executor;

	public TcpServerHandler(int threadPoolSize) {
		this.executor = ExecutorFactory.createRequestTaskExecutorAndInit(threadPoolSize);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		Request request = (Request) msg;
		if (log.isDebugEnabled()) {
			log.debug("recieve Request. guid:{} version:{} method:{} key:{} value:{}", request.getGuid(),
					request.getVersion(), request.getMethod(), request.getKey(), request.getValue());
		}
		RequestTask task = new RequestTask(request, ctx);
		executor.execute(task);
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		log.error("exceptionCaught", cause);
		ctx.close();
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		log.debug("连接上了");
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) {
		log.debug("断开连接了");
	}
}

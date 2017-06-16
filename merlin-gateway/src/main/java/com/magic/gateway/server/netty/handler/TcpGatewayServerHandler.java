package com.magic.gateway.server.netty.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.magic.executor.Executor;
import com.magic.executor.factory.ExecutorFactory;
import com.magic.gateway.server.dispatcher.task.GatewayRequestTask;
import com.magic.gateway.server.netty.request.GatewayRequest;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

@Sharable
public class TcpGatewayServerHandler extends ChannelInboundHandlerAdapter {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private Executor executor;

	public TcpGatewayServerHandler(int threadPoolSize) {
		this.executor = ExecutorFactory.createRequestExecutorAndInit(threadPoolSize);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		GatewayRequest request = (GatewayRequest) msg;
		if (log.isDebugEnabled()) {
			log.debug("recieve GatewayRequest. method:{} key:{} value:{}", request.getMethod(), request.getKey(),
					request.getValue());
		}
		GatewayRequestTask task = new GatewayRequestTask(request, ctx);
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

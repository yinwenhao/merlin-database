package com.magic.gateway.server.dispatcher.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.magic.gateway.server.dispatcher.GatewayDispatcher;
import com.magic.gateway.server.netty.request.GatewayRequest;
import com.magic.gateway.server.netty.request.GatewayResponse;

import io.netty.channel.ChannelHandlerContext;

public class GatewayRequestTask implements Runnable {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private ChannelHandlerContext ctx;

	private GatewayRequest request;

	private static GatewayDispatcher dispatcher;

	public GatewayRequestTask(GatewayRequest request, ChannelHandlerContext ctx) {
		this.request = request;
		this.ctx = ctx;
	}

	@Override
	public void run() {
		GatewayResponse response = null;
		try {
			response = dispatcher.getResult(request);
			if (response != null) {
				ctx.writeAndFlush(response);
			}
		} catch (Exception e) {
			log.error("gateway request task error", e);
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

	public static GatewayDispatcher getDispatcher() {
		return dispatcher;
	}

	public static void setDispatcher(GatewayDispatcher dispatcher) {
		GatewayRequestTask.dispatcher = dispatcher;
	}

}

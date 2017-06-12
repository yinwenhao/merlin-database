package com.magic.synchronize;

import com.magic.netty.request.Request;

import io.netty.channel.ChannelHandlerContext;

public class Sender {

	private ChannelHandlerContext ctx;

	public void send(Request request) {
		ctx.writeAndFlush(request);
	}

	public ChannelHandlerContext getCtx() {
		return ctx;
	}

	public void setCtx(ChannelHandlerContext ctx) {
		this.ctx = ctx;
	}

}

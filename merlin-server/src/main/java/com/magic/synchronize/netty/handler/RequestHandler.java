package com.magic.synchronize.netty.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.magic.synchronize.Sender;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

@Sharable
public class RequestHandler extends ChannelInboundHandlerAdapter {

	private Logger log = LoggerFactory.getLogger(getClass());

	private Sender sender;

	public RequestHandler(Sender sender) {
		this.sender = sender;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		log.info("channelActive: " + ctx.channel().toString());
		sender.setCtx(ctx);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {

	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {

	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		log.error("netty exceptionCaught: " + ctx.channel().toString(), cause);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) {
		log.info("channelInactive: " + ctx.channel().toString());
	}

}

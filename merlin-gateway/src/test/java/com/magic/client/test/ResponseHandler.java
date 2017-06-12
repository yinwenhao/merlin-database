package com.magic.client.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.magic.gateway.server.netty.request.GatewayResponse;

import io.netty.channel.ChannelHandler.Sharable;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 
 * @author yinwenhao
 *
 */
@Sharable
public class ResponseHandler extends ChannelInboundHandlerAdapter {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private Sender sender;

	public ResponseHandler(Sender sender) {
		this.sender = sender;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		sender.setCtx(ctx);
	}

	@Override
	public void channelRead(final ChannelHandlerContext ctx, Object message) throws Exception {
		GatewayResponse response = (GatewayResponse) message;
		sender.setResponse(response.getGuid(), response);
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
	public void channelInactive(ChannelHandlerContext ctx) {
		log.debug("断开连接了");
	}

	public Sender getSender() {
		return sender;
	}

	public void setSender(Sender sender) {
		this.sender = sender;
	}

}

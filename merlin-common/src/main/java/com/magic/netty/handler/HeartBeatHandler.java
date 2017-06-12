package com.magic.netty.handler;

import com.magic.constants.Constants;
import com.magic.netty.request.Request;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

@Sharable
public class HeartBeatHandler extends ChannelDuplexHandler {

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			IdleStateEvent e = (IdleStateEvent) evt;
			if (e.state() == IdleState.ALL_IDLE) {
				// 一直没有发送或收到数据，发送心跳
				ctx.writeAndFlush(new Request(Constants.HEART_BEAT));
			}
		}
	}
}

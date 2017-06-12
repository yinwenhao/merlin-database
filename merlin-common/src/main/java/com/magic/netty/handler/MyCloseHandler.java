package com.magic.netty.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

@Sharable
public class MyCloseHandler extends ChannelDuplexHandler {

	private final Logger log = LoggerFactory.getLogger(getClass());

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			IdleStateEvent e = (IdleStateEvent) evt;
			if (e.state() == IdleState.ALL_IDLE) {
				// 一直没有发送或收到数据，断开连接
				log.debug("idle too lang, close");
				ctx.close();
			}
		}
	}
}

package com.magic.netty.handler;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.magic.netty.MyTcpConstants;
import com.magic.netty.serial.InputFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 解码，基于长度的
 * 
 * @author yinwenhao
 *
 */
@Sharable
public class DecodeWithLengthFieldHandler extends ChannelInboundHandlerAdapter {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final InputFactory input;

	private final Class<?> clazz;

	public DecodeWithLengthFieldHandler(InputFactory input, Class<?> clazz) {
		this.input = input;
		this.clazz = clazz;
	}

	@Override
	public void channelRead(final ChannelHandlerContext ctx, Object message) throws Exception {
		ByteBuf buffer = ByteBuf.class.cast(message);
		buffer.skipBytes(MyTcpConstants.lengthFieldLength); // 包长度
		int protocolVersion = buffer.readByte(); // 协议版本号
		if (log.isDebugEnabled()) {
			log.debug("recieve Request. ProtocolVersion:" + protocolVersion);
		}
		buffer.skipBytes(MyTcpConstants.remainFieldLength); // 预留字节
		try (InputStream in = new ByteBufInputStream(buffer)) {
			ctx.fireChannelRead(input.input(in, this.clazz));
		} finally {
			buffer.release();
		}
	}

}

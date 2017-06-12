package com.magic.netty.handler;

import com.magic.netty.MyTcpConstants;
import com.magic.netty.listener.ExceptionListener;
import com.magic.netty.serial.OutputFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

/**
 * 编码，基于分隔符的
 * 
 * @author yinwenhao
 *
 */
@Sharable
public class EncodeDelimiterBasedHandler extends ChannelOutboundHandlerAdapter {

	private final OutputFactory output;

	public EncodeDelimiterBasedHandler(OutputFactory output) {
		this.output = output;
	}

	@Override
	public void write(ChannelHandlerContext ctx, final Object msg, ChannelPromise promise) throws Exception {
		ByteBuf buffer = ctx.alloc().buffer();
		try (ByteBufOutputStream bbos = new ByteBufOutputStream(buffer)) {
			bbos.writeByte(output.getProtocolVersion());
			buffer.writeZero(MyTcpConstants.remainFieldLength); // 预留的字节
			this.output.output(bbos, msg);
			bbos.write(MyTcpConstants.DELIMITER_BYTES);
			ctx.writeAndFlush(buffer).addListener(ExceptionListener.getInstance());
		} finally {
			buffer.release();
		}
	}

}

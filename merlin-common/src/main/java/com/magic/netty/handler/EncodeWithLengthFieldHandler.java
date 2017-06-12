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
 * 编码，基于长度的
 * 
 * @author yinwenhao
 *
 */
@Sharable
public class EncodeWithLengthFieldHandler extends ChannelOutboundHandlerAdapter {

	private final OutputFactory output;

	public EncodeWithLengthFieldHandler(OutputFactory output) {
		this.output = output;
	}

	@Override
	public void write(ChannelHandlerContext ctx, final Object msg, ChannelPromise promise) throws Exception {
		ByteBuf buffer = ctx.alloc().buffer();
		try (ByteBufOutputStream bbos = new ByteBufOutputStream(buffer)) {
			buffer.writeZero(MyTcpConstants.lengthFieldLength); // 包大小的存放位置
			bbos.writeByte(output.getProtocolVersion());
			buffer.writeZero(MyTcpConstants.remainFieldLength); // 预留的字节
			this.output.output(bbos, msg);
			buffer.setInt(0, buffer.writerIndex() - MyTcpConstants.lengthFieldLength); // 如果更改MyTcpConstants.lengthFieldLength的值，需要改这个地方
			ctx.writeAndFlush(buffer).addListener(ExceptionListener.getInstance());
		} finally {
			buffer.release();
		}
	}

}

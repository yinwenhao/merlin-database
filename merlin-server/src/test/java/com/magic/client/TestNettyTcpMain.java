package com.magic.client;

import org.apache.log4j.PropertyConfigurator;

import com.magic.netty.MyTcpConstants;
import com.magic.netty.handler.DecodeWithLengthFieldHandler;
import com.magic.netty.handler.EncodeWithLengthFieldHandler;
import com.magic.netty.request.Response;
import com.magic.netty.serial.InputOutputFactory;
import com.magic.netty.serial.impl.JsonSerialFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;

public class TestNettyTcpMain {

	static final boolean SSL = System.getProperty("ssl") != null;
	static final String HOST = System.getProperty("host", "127.0.0.1");
	static final int PORT = Integer.parseInt(System.getProperty("port", "7865"));
	static final int SIZE = Integer.parseInt(System.getProperty("size", "256"));

	public static void main(String[] args) throws Exception {
		PropertyConfigurator.configureAndWatch(System.getProperty("confPath") + "/log4j.properties", 60 * 1000);

		InputOutputFactory a = new JsonSerialFactory();

		// JsonSerialFactory aa = new JsonSerialFactory();

		final ChannelOutboundHandlerAdapter encoder = new EncodeWithLengthFieldHandler(a);

		final ChannelInboundHandlerAdapter decoder = new DecodeWithLengthFieldHandler(a, Response.class);

		// Configure SSL.git
		// final SslContext sslCtx;
		// if (SSL) {
		// sslCtx =
		// SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
		// } else {
		// sslCtx = null;
		// }

		// Configure the client.
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap b = new Bootstrap();
			b.group(group).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
				@Override
				public void initChannel(SocketChannel ch) throws Exception {
					ChannelPipeline p = ch.pipeline();
					p.addLast("splitDecoder",
							new DelimiterBasedFrameDecoder(MyTcpConstants.maxFrameLength, MyTcpConstants.DELIMITER));
					p.addLast("encoder", encoder);
					p.addLast("decoder", decoder);
					p.addLast("actionHandler", new EchoClientHandler());
				}
			});

			// Start the client.
			ChannelFuture f = b.connect(HOST, PORT).sync();

			// Wait until the connection is closed.
			f.channel().closeFuture().sync();
		} finally {
			// Shut down the event loop to terminate all threads.
			group.shutdownGracefully();
		}
	}
}

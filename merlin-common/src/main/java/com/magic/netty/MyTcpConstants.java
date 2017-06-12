package com.magic.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class MyTcpConstants {

	/** 检测时间（超过这个时间没有读到或写出数据，则断开连接，秒） */
	public static final int idleSecondsToHeartBeat = 10;

	/** 检测时间（超过这个时间没有读到或写出数据，则断开连接，秒） */
	public static final int secondsToIdle = 300;

	/** tcp包内容的长度（字节），如果要修改这个值，需要检查所有引用的地方，有很多writeShort和readShort */
	public static final int lengthFieldLength = 4;

	/** tcp包头预留的字节数 */
	public static final int remainFieldLength = 2;

	/**
	 * 最大包长度
	 */
	public static final int maxFrameLength = 10 * 1024 * 1024;

	/** tcp协议版本号的长度（字节） */
	public static final int protocolVersionLength = 1;

	/** tcp包头部的长度（字节） */
	public static final int headLength = remainFieldLength + protocolVersionLength;

	/**
	 * 拆包用的分隔符byte数组
	 */
	public static final byte[] DELIMITER_BYTES = "-$_".getBytes();

	/**
	 * 拆包用的分隔符
	 */
	public static final ByteBuf DELIMITER = Unpooled.copiedBuffer(DELIMITER_BYTES);

}

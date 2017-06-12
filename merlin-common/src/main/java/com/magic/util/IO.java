package com.magic.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/** Utility class for I/O operations */
public class IO {

	public static long read(FileChannel ch, ByteBuffer bb, final long startPosition) throws IOException {
		long position = startPosition;
		while (bb.hasRemaining()) {
			int read = ch.read(bb, position);
			if (read >= 0) {
				position += read;
				if (read == 0) {
					Thread.yield();
				}
			} else {
				return 0;
			}
		}
		return position - startPosition;
	}

	public static long writeFullyForce(FileChannel ch, ByteBuffer buffer) throws IOException {
		synchronized (ch) {
			long w = 0;
			long len = buffer.remaining();
			while (w < len) {
				long ww = ch.write(buffer);
				if (ww > 0) {
					w += ww;
				} else if (ww == 0) {
					Thread.yield();
				} else {
					return w;
				}
			}
			ch.force(false);// 是否将文件新增内容强制刷到磁盘
			return w;
		}
	}

	public static long writeFullyForce(FileChannel ch, ByteBuffer[] vec) throws IOException {
		synchronized (ch) {
			long len = length(vec);
			long w = 0;
			while (w < len) {
				long ww = ch.write(vec);
				if (ww > 0) {
					w += ww;
				} else if (ww == 0) {
					Thread.yield();
				} else {
					return w;
				}
			}
			ch.force(false);// 是否将文件新增内容强制刷到磁盘
			return w;
		}
	}

	private static long length(ByteBuffer[] vec) {
		long length = 0;
		for (int i = 0; i < vec.length; i++) {
			length += vec[i].remaining();
		}
		return length;
	}

}

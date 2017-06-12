package com.magic.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.magic.netty.request.Request;

import io.netty.channel.ChannelHandlerContext;

public class CommandManager implements Runnable {

	private ChannelHandlerContext ctx;

	public CommandManager(ChannelHandlerContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public void run() {
		// System.out
		// .println("input command. Example:"
		// + "\r\nlogin login token=String:12345678901234567890123456789012999"
		// + "\r\ncard changeCardsInHand changeIndexString=String:0,1
		// turn=int:0"
		// + "\r\ncard useCard i=int:0 location=int:0"
		// + "\r\nturn endTurn turn=int:1");
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			boolean done = false;
			while (!done) {
				System.out.print("> ");

				String command = in.readLine().trim();

				String[] parts = command.split("\\s");
				if (parts.length == 0) {
					continue;
				}

				if (parts[0].equals("q") || parts[0].equals("quit")) {
					done = true;
					break;
				}

				Request request = new Request(parts[0], parts[1], parts.length > 2 ? parts[2] : null,
						System.currentTimeMillis());
				ctx.writeAndFlush(request);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

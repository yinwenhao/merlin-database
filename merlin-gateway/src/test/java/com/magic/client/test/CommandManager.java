package com.magic.client.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class CommandManager {

	private Client client;

	public CommandManager(Client client) {
		this.client = client;
	}

	public void begin() {
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		boolean done = false;
		while (!done) {
			try {
				System.out.print("> ");

				String command = in.readLine().trim();
				if (command.isEmpty()) {
					continue;
				}

				String[] parts = command.split("\\s");
				if (parts.length == 0) {
					continue;
				}

				if (parts[0].equals("q") || parts[0].equals("quit")) {
					done = true;
					return;
				}

				switch (parts[0]) {
				case "get":
					System.out.println(client.get(parts[1]));
					break;
				case "set":
					if (parts.length > 3) {
						client.setWithExpire(parts[1], parts[2], Integer.valueOf(parts[3]));
					} else {
						client.set(parts[1], parts[2]);
					}
					System.out.println("ok");
					break;
				case "delete":
					client.delete(parts[1]);
					System.out.println("ok");
					break;
				default:
					System.out.println("method error");
				}
			} catch (Exception e) {
				System.out.println(e.getMessage());
				continue;
			}
		}
	}
}

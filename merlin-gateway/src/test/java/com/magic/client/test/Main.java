package com.magic.client.test;

import org.apache.log4j.PropertyConfigurator;

public class Main {

	public static void main(String[] args) throws Exception {
		PropertyConfigurator.configureAndWatch(System.getProperty("confPath") + "/log4j.properties", 60 * 1000);
		Sender sender = new Sender();
		Client client = new Client(sender);
		NettyClient nc = new NettyClient("127.0.0.1", 5612, sender, 1, 1000);
		nc.startClient();

		CommandManager cm = new CommandManager(client);
		cm.begin();

		nc.shutdown();
	}

}

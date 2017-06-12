package com.magic.gateway.server;

import java.io.File;

import org.apache.log4j.PropertyConfigurator;

import com.magic.gateway.impl.Client;
import com.magic.gateway.options.GatewayOptions;
import com.magic.gateway.server.dispatcher.GatewayDispatcher;
import com.magic.gateway.server.dispatcher.GatewayDispatcherImpl;
import com.magic.gateway.server.dispatcher.task.GatewayRequestTask;
import com.magic.gateway.server.netty.NettyServer;
import com.magic.server.NetServer;
import com.magic.service.domain.AllServiceInstance;
import com.magic.util.Json;

public class Main {

	public static void main(String[] args) throws Exception {
		PropertyConfigurator.configureAndWatch(System.getProperty("confPath") + "/log4j.properties", 60 * 1000);

		File conf = new File(System.getProperty("confPath") + "/application.properties");
		GatewayOptions opts = new GatewayOptions(conf);

		File jsonFile = new File(System.getProperty("confPath") + "/services.json");
		opts.allServiceInstance = Json.readValue(jsonFile, AllServiceInstance.class);

		Client client = new Client(opts);
		GatewayDispatcher dispatcher = new GatewayDispatcherImpl(client);
		GatewayRequestTask.setDispatcher(dispatcher);

		NetServer netServer = new NettyServer(opts);
		netServer.startServer();
		netServer.sync();
	}

}

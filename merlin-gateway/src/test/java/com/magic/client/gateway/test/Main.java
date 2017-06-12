package com.magic.client.gateway.test;

import java.io.File;

import org.apache.log4j.PropertyConfigurator;

import com.magic.gateway.impl.Client;
import com.magic.gateway.options.GatewayOptions;
import com.magic.service.domain.AllServiceInstance;
import com.magic.util.Json;

public class Main {

	public static void main(String[] args) throws Exception {
		PropertyConfigurator.configureAndWatch(System.getProperty("confPath") + "/log4j.properties", 60 * 1000);
		GatewayOptions opts = new GatewayOptions();

		File jsonFile = new File(System.getProperty("confPath") + "/services.json");
		opts.allServiceInstance = Json.readValue(jsonFile, AllServiceInstance.class);

		Client client = new Client(opts);
		CommandManager commandManager = new CommandManager(client);
		commandManager.begin();

		client.close();
	}

}

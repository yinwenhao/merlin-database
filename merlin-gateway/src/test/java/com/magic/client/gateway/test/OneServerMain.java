package com.magic.client.gateway.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.PropertyConfigurator;

import com.magic.gateway.impl.Client;
import com.magic.gateway.options.GatewayOptions;
import com.magic.service.domain.AllServiceInstance;
import com.magic.service.domain.MagicServiceInstance;

public class OneServerMain {

	public static void main(String[] args) throws Exception {
		PropertyConfigurator.configureAndWatch(System.getProperty("confPath") + "/log4j.properties", 60 * 1000);
		GatewayOptions opts = new GatewayOptions();
		opts.readNum = 1;

		opts.allServiceInstance = new AllServiceInstance();
		Map<String, List<MagicServiceInstance>> services = new HashMap<String, List<MagicServiceInstance>>();
		List<MagicServiceInstance> l = new ArrayList<MagicServiceInstance>();
		l.add(new MagicServiceInstance("127.0.0.1", Integer.valueOf(System.getProperty("serverPort", "7865"))));
		services.put("md-server#0", l);
		opts.allServiceInstance.setServices(services);

		Client client = new Client(opts);
		CommandManager commandManager = new CommandManager(client);
		commandManager.begin();

		client.close();
	}

}

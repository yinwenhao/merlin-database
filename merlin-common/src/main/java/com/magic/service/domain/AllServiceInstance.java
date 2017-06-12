package com.magic.service.domain;

import java.util.List;
import java.util.Map;

public class AllServiceInstance {

	private MagicServiceInstance selfInstance;

	private Map<String, List<MagicServiceInstance>> services;

	public MagicServiceInstance getSelfInstance() {
		return selfInstance;
	}

	public void setSelfInstance(MagicServiceInstance selfInstance) {
		this.selfInstance = selfInstance;
	}

	public Map<String, List<MagicServiceInstance>> getServices() {
		return services;
	}

	public void setServices(Map<String, List<MagicServiceInstance>> services) {
		this.services = services;
	}

}

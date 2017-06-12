package com.magic.service.impl;

import java.io.IOException;
import java.util.Collection;

import com.magic.service.Register;
import com.magic.service.domain.MagicServiceInstance;
import com.magic.service.domain.AllServiceInstance;

public class DefaultRegisterImpl implements Register {

	private AllServiceInstance allServiceInstance;

	public DefaultRegisterImpl(AllServiceInstance allServiceInstance) throws IOException {
		this.allServiceInstance = allServiceInstance;
	}

	@Override
	public void regist() throws Exception {
		// do nothing
	}

	@Override
	public void unregist() throws Exception {
		// do nothing
	}

	@Override
	public Collection<String> queryForNames() throws Exception {
		return allServiceInstance.getServices().keySet();
	}

	@Override
	public Collection<MagicServiceInstance> queryForInstances(String serviceName) throws Exception {
		return allServiceInstance.getServices().get(serviceName);
	}

	@Override
	public MagicServiceInstance getSelfInstance() {
		return this.allServiceInstance.getSelfInstance();
	}

	@Override
	public void close() {
		// do nothing
	}

}

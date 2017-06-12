package com.magic.service;

import java.util.Collection;

import com.magic.service.domain.MagicServiceInstance;

public interface Register {

	public void regist() throws Exception;

	public void unregist() throws Exception;

	public Collection<String> queryForNames() throws Exception;

	public Collection<MagicServiceInstance> queryForInstances(String serviceName) throws Exception;

	public MagicServiceInstance getSelfInstance();

	public void close();

}

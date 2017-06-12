package com.magic.service.impl;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.UriSpec;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.magic.service.Register;
import com.magic.service.domain.InstanceDetails;
import com.magic.service.domain.MagicServiceInstance;

public class ZookeeperRegisterImpl implements Register {

	private Logger log = LoggerFactory.getLogger(getClass());

	private CuratorFramework client;

	private final ServiceDiscovery<InstanceDetails> serviceDiscovery;
	private final ServiceInstance<InstanceDetails> thisInstance;

	private MagicServiceInstance selfInstance;

	/**
	 * 用于服务端
	 * 
	 * @param serviceName
	 * @param port
	 * @param zkList
	 * @param baseSleepTimeMs
	 * @param maxRetries
	 * @param zkBasePath
	 * @throws Exception
	 */
	public ZookeeperRegisterImpl(String serviceName, int port, String zkList, int baseSleepTimeMs, int maxRetries,
			String zkBasePath) throws Exception {
		UriSpec uriSpec = new UriSpec(serviceName + " [{address}:{port}]");

		thisInstance = ServiceInstance.<InstanceDetails>builder().name(serviceName).payload(new InstanceDetails())
				.port(port).uriSpec(uriSpec).build();

		JsonInstanceSerializer<InstanceDetails> serializer = new JsonInstanceSerializer<InstanceDetails>(
				InstanceDetails.class);

		client = CuratorFrameworkFactory.newClient(zkList, new ExponentialBackoffRetry(baseSleepTimeMs, maxRetries));
		client.start();
		serviceDiscovery = ServiceDiscoveryBuilder.builder(InstanceDetails.class).client(client).basePath(zkBasePath)
				.serializer(serializer).thisInstance(thisInstance).build();

		this.selfInstance = new MagicServiceInstance(thisInstance.getAddress(), thisInstance.getPort());
	}

	/**
	 * 用于客户端
	 * 
	 * @param zkList
	 * @param zkBasePath
	 * @throws Exception
	 */
	public ZookeeperRegisterImpl(String zkList, String zkBasePath, int baseSleepTimeMs, int maxRetries)
			throws Exception {
		client = CuratorFrameworkFactory.newClient(zkList, new ExponentialBackoffRetry(baseSleepTimeMs, maxRetries));
		client.start();

		JsonInstanceSerializer<InstanceDetails> serializer = new JsonInstanceSerializer<InstanceDetails>(
				InstanceDetails.class);

		serviceDiscovery = ServiceDiscoveryBuilder.builder(InstanceDetails.class).client(client).basePath(zkBasePath)
				.serializer(serializer).build();
		serviceDiscovery.start();
		thisInstance = null;
	}

	@Override
	public void regist() throws Exception {
		serviceDiscovery.start();
	}

	@Override
	public void unregist() throws Exception {
		serviceDiscovery.unregisterService(thisInstance);
		log.info("unregisterService bitcask server: {} details-[{}]", thisInstance.buildUriSpec(),
				thisInstance.getPayload().getDescription());
	}

	@Override
	public Collection<String> queryForNames() throws Exception {
		return serviceDiscovery.queryForNames();
	}

	@Override
	public Collection<MagicServiceInstance> queryForInstances(String serviceName) throws Exception {
		Collection<MagicServiceInstance> result = new ArrayList<MagicServiceInstance>();
		for (ServiceInstance<InstanceDetails> si : serviceDiscovery.queryForInstances(serviceName)) {
			result.add(new MagicServiceInstance(si.getAddress(), si.getPort()));
		}
		return result;
	}

	@Override
	public MagicServiceInstance getSelfInstance() {
		return selfInstance;
	}

	public CuratorFramework getClient() {
		return client;
	}

	public void setClient(CuratorFramework client) {
		this.client = client;
	}

	@Override
	public void close() {
		client.close();
	}

}

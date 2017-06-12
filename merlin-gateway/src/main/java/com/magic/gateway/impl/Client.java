package com.magic.gateway.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.magic.constants.Constants;
import com.magic.gateway.MagicClient;
import com.magic.gateway.exception.BaseException;
import com.magic.gateway.options.GatewayOptions;
import com.magic.service.Register;
import com.magic.service.domain.MagicServiceInstance;
import com.magic.service.impl.DefaultRegisterImpl;
import com.magic.service.impl.ZookeeperRegisterImpl;

public class Client implements MagicClient, Runnable {

	private Logger log = LoggerFactory.getLogger(getClass());

	private Register register;

	private ShardClientManager shardClientManager;

	private GatewayOptions opts;

	public Client(GatewayOptions opts) throws Exception {
		this.opts = opts;
		if (opts.allServiceInstance == null) {
			// 使用zk注册器
			register = new ZookeeperRegisterImpl(opts.zkList, opts.zkBasePath, opts.baseSleepTimeMs, opts.maxRetries);
		} else {
			// 使用配置文件注册器
			register = new DefaultRegisterImpl(opts.allServiceInstance);
		}
		listServer();
	}

	private void listServer() throws Exception {
		Collection<String> serviceNames = register.queryForNames();
		log.info("find service shard number: " + serviceNames.size());
		Map<Integer, ShardClient> shardClientMap = new HashMap<Integer, ShardClient>();
		for (String serviceName : serviceNames) {
			Collection<MagicServiceInstance> instances = register.queryForInstances(serviceName);
			if (instances.size() > 0) {
				for (MagicServiceInstance instance : instances) {
					outputInstance(serviceName, instance);
				}
				shardClientMap.put(serviceNameToServiceIndex(serviceName),
						new ShardClient(serviceName, instances, opts));
			} else {
				log.info("no service instance for {}", serviceName);
			}
		}
		this.shardClientManager = new ShardClientManager(shardClientMap);
	}

	private int serviceNameToServiceIndex(String serviceName) {
		return Integer.valueOf(serviceName.replace(Constants.SERVICE_NAME_HEAD, ""));
	}

	@Override
	public String get(String key) throws BaseException, InterruptedException {
		return this.shardClientManager.get(key);
	}

	@Override
	public void set(String key, String value) throws BaseException, InterruptedException {
		setWithExpire(key, value, Constants.EXPIRE_TIME_DEFAULT);
	}

	@Override
	public void setWithExpire(String key, String value, long expire)
			throws BaseException, InterruptedException {
		this.shardClientManager.setWithExpire(key, value, expire);
	}

	@Override
	public void delete(String key) throws BaseException, InterruptedException {
		this.shardClientManager.delete(key);
	}

	private void outputInstance(String serviceName, MagicServiceInstance instance) {
		log.info("find bitcask server: {}:{}", instance.getAddress(), instance.getPort());
	}

	@Override
	public void run() {
		try {
			listServer();
		} catch (Exception e) {
			log.error("listServer error", e);
		}
	}

	@Override
	public void close() {
		shardClientManager.close();
		register.close();
	}

}

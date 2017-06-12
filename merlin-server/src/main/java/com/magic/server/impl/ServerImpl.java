package com.magic.server.impl;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.magic.bitcask.core.BitCaskServer;
import com.magic.bitcask.core.factory.BitCaskFactory;
import com.magic.bitcask.core.factory.impl.BitCaskFactoryImpl;
import com.magic.server.NetServer;
import com.magic.server.Server;
import com.magic.server.dispatcher.Dispatcher;
import com.magic.server.dispatcher.DispatcherImpl;
import com.magic.server.dispatcher.task.RequestTask;
import com.magic.server.netty.NettyServer;
import com.magic.server.options.ServerOptions;
import com.magic.service.Register;
import com.magic.service.impl.DefaultRegisterImpl;
import com.magic.service.impl.ZookeeperRegisterImpl;
import com.magic.synchronize.Synchronizer;
import com.magic.synchronize.SynchronizerImpl;
import com.magic.timer.ScheduledExecutorServiceTimer;
import com.magic.timer.Timer;

public class ServerImpl implements Server, Runnable {

	private Logger log = LoggerFactory.getLogger(getClass());

	private Register register;

	private final NetServer netServer;

	private final Synchronizer synchronizer;

	private final Timer timer;

	private final BitCaskServer bitcask;

	public ServerImpl(ServerOptions opts) throws Exception {
		log.info("magic database initializing...");

		BitCaskFactory bitCaskFactory = new BitCaskFactoryImpl();
		bitcask = bitCaskFactory.createBitCask(new File(opts.bitcaskDir), opts.bitCaskOptions);

		netServer = new NettyServer(opts);

		if (opts.allServiceInstance == null) {
			// 使用zk注册器
			register = new ZookeeperRegisterImpl(opts.getServiceName(), opts.port, opts.zkList, opts.baseSleepTimeMs,
					opts.maxRetries, opts.zkBasePath);
		} else {
			// 使用配置文件注册器
			register = new DefaultRegisterImpl(opts.allServiceInstance);
		}

		synchronizer = new SynchronizerImpl(register, opts, bitcask);
		Dispatcher dispatcher = new DispatcherImpl(bitcask, synchronizer);
		RequestTask.setDispatcher(dispatcher);

		timer = ScheduledExecutorServiceTimer.getInstance();
	}

	@Override
	public void start() throws Exception {
		log.info("magic database starting...");
		Runtime.getRuntime().addShutdownHook(new Thread(this));
		bitcask.scheduleMerge();
		netServer.startServer();
		register.regist();
		synchronizer.start();
		log.info("magic database started");
		netServer.sync();
	}

	@Override
	public void run() {
		try {
			register.unregist();
			synchronizer.shutdown();
			timer.shutdown();
			netServer.shutdown();
			bitcask.close();
		} catch (Exception e) {
			log.error("server shutdown error", e);
		}
	}

}

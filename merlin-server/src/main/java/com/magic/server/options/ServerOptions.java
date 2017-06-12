package com.magic.server.options;

import java.io.File;
import java.io.IOException;

import com.magic.bitcask.options.BitCaskOptions;
import com.magic.constants.Constants;
import com.magic.service.domain.AllServiceInstance;
import com.magic.util.Options;

public class ServerOptions {

	public String bitcaskDir = "/Users/yinwenhao/workspace/magic-database/magic-bitcask"; // 数据存储目录
	public int shardIndex = 0; // 分片id，从0开始，依次加一
	public int port = 7865; // 服务端口

	public String zkBasePath = "/magic/database";

	public String zkList = "10.128.8.57:2181";
	public int baseSleepTimeMs = 1000;
	public int maxRetries = 3; // zk注册服务时最大重试次数
	public long log4jWatchDelay = 60 * 1000; // log4j 自动扫描配置文件的时间间隔（毫秒）

	public int threadNumForOneChannel = 1; // 连接其他bitcask服务器的netty客户端线程数

	public long periodMilliSeconds = 5 * 1000; // bitcask间同步的时间间隔

	public long periodMilliSecondsForBind = 1000; // netty绑定端口的重试时间间隔

	public long periodMilliSecondsForReconnect = 1000; // netty客户端重连的时间间隔

	public BitCaskOptions bitCaskOptions;

	public AllServiceInstance allServiceInstance = null; // 配置文件中所有服务的信息

	public String getServiceName() {
		return Constants.SERVICE_NAME_HEAD + shardIndex;
	}

	public ServerOptions() throws IllegalArgumentException, IllegalAccessException, IOException {
		this(null);
	}

	public ServerOptions(File conf) throws IllegalArgumentException, IllegalAccessException, IOException {
		if (conf == null) {
			this.bitCaskOptions = new BitCaskOptions();
			return;
		}
		this.bitCaskOptions = new BitCaskOptions(conf);
		Options.usePropertiesFromConfFile(this, conf);
		Options.useSystemProperties(this);
	}

}

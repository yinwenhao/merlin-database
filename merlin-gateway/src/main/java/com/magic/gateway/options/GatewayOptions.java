package com.magic.gateway.options;

import java.io.File;
import java.io.IOException;

import com.magic.service.domain.AllServiceInstance;
import com.magic.util.Options;

public class GatewayOptions {

	public String zkBasePath = "/magic/database";

	public int threadPoolSize = 4; // 业务处理线程池的大小

	public String zkList = "10.128.8.57:2181";
	public int baseSleepTimeMs = 1000;
	public int maxRetries = 3; // zk注册服务时最大重试次数

	public long timeoutMilliseconds = 3 * 1000; // 等待bitcask服务端响应的超时毫秒数

	public int threadNumForOneChannel = 1;

	public long periodMilliSeconds = 30000; // 扫描zk获取服务列表的时间间隔（毫秒）

	public AllServiceInstance allServiceInstance = null; // 配置文件中所有服务的信息

	public long periodMilliSecondsForReconnect = 1000; // netty重连的重试时间间隔

	public boolean checkBeforeWrite = false; // 写入前是否先检查服务器状态。开启的话会让性能降低，但脏读的可能会几乎降为0
	public int readNum = 2; // 最少获得返回的节点数
	public int readSameNum = 2; // 最少获得相同返回的节点数
	public int mode = 0; // 模式，0普通模式，1读R个相同的值

	// 下面为gateway server的配置
	public int port = 5612;

	public GatewayOptions() throws IllegalArgumentException, IllegalAccessException, IOException {
		this(null);
	}

	public GatewayOptions(File conf) throws IllegalArgumentException, IllegalAccessException, IOException {
		if (conf == null) {
			return;
		}
		Options.usePropertiesFromConfFile(this, conf);
		Options.useSystemProperties(this);
	}

}

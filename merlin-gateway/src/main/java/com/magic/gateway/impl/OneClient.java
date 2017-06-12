package com.magic.gateway.impl;

import com.magic.gateway.exception.BaseException;
import com.magic.gateway.netty.handler.ResponseHandler;
import com.magic.gateway.options.GatewayOptions;
import com.magic.gateway.sender.Sender;
import com.magic.gateway.sender.ShardResponseFuture;
import com.magic.netty.client.NettyClient;
import com.magic.netty.request.Request;
import com.magic.service.domain.MagicServiceInstance;

public class OneClient {

	private MagicServiceInstance serviceInstance;

	private NettyClient nettyClient;

	private Sender sender;

	private GatewayOptions opts;

	public OneClient(MagicServiceInstance serviceInstance, GatewayOptions opts) {
		this.opts = opts;
		this.serviceInstance = serviceInstance;
		this.sender = new Sender();
		this.nettyClient = new NettyClient(new ResponseHandler(sender), serviceInstance.getAddress(),
				this.serviceInstance.getPort(), this.opts.threadNumForOneChannel,
				this.opts.periodMilliSecondsForReconnect);
		this.nettyClient.startClient();
	}

	public void shutdown() {
		this.nettyClient.shutdown();
	}

	public MagicServiceInstance getServiceInstance() {
		return serviceInstance;
	}

	public void setServiceInstance(MagicServiceInstance serviceInstance) {
		this.serviceInstance = serviceInstance;
	}

	public NettyClient getNettyClient() {
		return nettyClient;
	}

	public void setNettyClient(NettyClient nettyClient) {
		this.nettyClient = nettyClient;
	}

	public Sender getSender() {
		return sender;
	}

	public void setSender(Sender sender) {
		this.sender = sender;
	}

	public void sendRequest(Request request, ShardResponseFuture future) throws BaseException {
		this.sender.sendRequest(request, future);
	}

}

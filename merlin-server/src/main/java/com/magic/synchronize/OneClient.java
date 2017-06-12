package com.magic.synchronize;

import com.magic.netty.client.NettyClient;
import com.magic.netty.request.Request;
import com.magic.server.options.ServerOptions;
import com.magic.service.domain.MagicServiceInstance;
import com.magic.synchronize.netty.handler.RequestHandler;

public class OneClient {

	private MagicServiceInstance serviceInstance;

	private NettyClient nettyClient;

	private Sender sender;

	private ServerOptions opts;

	public OneClient(MagicServiceInstance serviceInstance, ServerOptions opts) {
		this.opts = opts;
		this.serviceInstance = serviceInstance;
		this.sender = new Sender();
		this.nettyClient = new NettyClient(new RequestHandler(sender), serviceInstance.getAddress(),
				this.serviceInstance.getPort(), this.opts.threadNumForOneChannel,
				this.opts.periodMilliSecondsForReconnect);
		this.nettyClient.startClient();
	}

	public String getAddressAndPort() {
		return serviceInstance.getAddress() + serviceInstance.getPort();
	}

	public void send(Request request) {
		sender.send(request);
	}

	public void shutdown() {
		this.nettyClient.shutdown();
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

	public MagicServiceInstance getServiceInstance() {
		return serviceInstance;
	}

	public void setServiceInstance(MagicServiceInstance serviceInstance) {
		this.serviceInstance = serviceInstance;
	}

}

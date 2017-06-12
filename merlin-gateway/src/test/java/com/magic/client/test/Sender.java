package com.magic.client.test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.magic.gateway.exception.UnconnectException;
import com.magic.gateway.sender.ResponseFuture;
import com.magic.gateway.server.netty.request.GatewayRequest;
import com.magic.gateway.server.netty.request.GatewayResponse;

import io.netty.channel.ChannelHandlerContext;

public class Sender {

	private Map<String, ResponseFuture<GatewayResponse>> futureMap = new HashMap<String, ResponseFuture<GatewayResponse>>();

	private ChannelHandlerContext ctx;

	/**
	 * 同步发送请求并获取结果
	 * 
	 * @param request
	 * @param timeout
	 * @param unit
	 * @return
	 * @throws Exception
	 */
	public GatewayResponse sendRequest(GatewayRequest request, long timeout, TimeUnit unit) throws Exception {
		if (ctx == null) {
			throw new UnconnectException();
		}
		ResponseFuture<GatewayResponse> future = new ResponseFuture<GatewayResponse>();
		futureMap.put(request.getGuid(), future);
		ctx.writeAndFlush(request);
		GatewayResponse result = future.get(timeout, unit);
		futureMap.remove(request.getGuid());
		return result;
	}

	public void setResponse(String guid, GatewayResponse response) {
		if (futureMap.containsKey(guid)) {
			futureMap.get(guid).setResponse(response);
		}
	}

	public ChannelHandlerContext getCtx() {
		return ctx;
	}

	public void setCtx(ChannelHandlerContext ctx) {
		this.ctx = ctx;
	}

}

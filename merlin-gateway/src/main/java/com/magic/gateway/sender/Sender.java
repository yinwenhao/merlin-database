package com.magic.gateway.sender;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.magic.gateway.exception.UnconnectException;
import com.magic.netty.request.Request;
import com.magic.netty.request.Response;

import io.netty.channel.ChannelHandlerContext;

public class Sender {

	private Map<String, ResponseFuture<Response>> futureMap = new HashMap<String, ResponseFuture<Response>>();

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
	public Response sendRequest(Request request, long timeout, TimeUnit unit) throws Exception {
		if (ctx == null) {
			throw new UnconnectException();
		}
		ResponseFuture<Response> future = new ResponseFuture<Response>();
		futureMap.put(request.getGuid(), future);
		ctx.writeAndFlush(request);
		Response result = future.get(timeout, unit);
		futureMap.remove(request.getGuid());
		return result;
	}

	/**
	 * 异步获取结果（分片的总结果）
	 * 
	 * @param request
	 * @return
	 * @throws UnconnectException
	 */
	public void sendRequest(Request request, ShardResponseFuture future) throws UnconnectException {
		if (ctx == null) {
			throw new UnconnectException();
		}
		SenderManager.ensureFutureInMap(request.getGuid(), future);
		ctx.writeAndFlush(request);
	}

	public void setResponse(String guid, Response response) {
		if (futureMap.containsKey(guid)) {
			futureMap.get(guid).setResponse(response);
		}
		ShardResponseFuture srf = SenderManager.getShardResponseFuture(guid);
		if (srf != null) {
			srf.setOneResponse(response);
		}
	}

	public ChannelHandlerContext getCtx() {
		return ctx;
	}

	public void setCtx(ChannelHandlerContext ctx) {
		this.ctx = ctx;
	}

}

package com.magic.server.dispatcher;

import com.magic.bitcask.core.BitCask;
import com.magic.bitcask.entity.BitCaskValue;
import com.magic.bitcask.exception.WrongMethodException;
import com.magic.constants.Constants;
import com.magic.netty.request.Request;
import com.magic.netty.request.Response;
import com.magic.synchronize.Synchronizer;

public class DispatcherImpl implements Dispatcher {

	private BitCask bitcask;

	private Synchronizer synchronizer;

	public DispatcherImpl(BitCask bitcask, Synchronizer synchronizer) {
		this.bitcask = bitcask;
		this.synchronizer = synchronizer;
	}

	@Override
	public Response getResult(Request request) throws Exception {
		String method = request.getMethod().toLowerCase();
		Response response = new Response();
		response.setGuid(request.getGuid());
		response.setKey(request.getKey());
		switch (method) {
		case Constants.GET:
			BitCaskValue bcv = bitcask.get(request.getKey());
			response.setValue(bcv.getValue());
			response.setVersion(bcv.getVersion());
			break;
		case Constants.BEFORE_SET:
			response.setValue(Constants.RESPONSE_OK);
			break;
		case Constants.SET:
			bitcask.setWithExpire(request.getKey(), request.getValue(), request.getVersion(),
					request.getExpire());
			response.setValue(Constants.RESPONSE_OK);
			response.setVersion(request.getVersion());
			break;
		case Constants.DELETE:
			bitcask.delete(request.getKey(), request.getVersion());
			response.setValue(Constants.RESPONSE_OK);
			response.setVersion(request.getVersion());
			break;
		case Constants.GOSSIP:
			// 同步节点信息：hash对比
			synchronizer.checkHash(request.getKey(), request.getValue());
			response = null;
			break;
		case Constants.GOSSIP_REQUIRE:
			// 同步节点信息：请求发送GOSSIP_SET
			synchronizer.needValue(request.getKey());
			response = null;
			break;
		case Constants.GOSSIP_SET:
			// 同步节点信息：设置新值
			synchronizer.set(request.getKey(), request.getValue(), request.getVersion(), request.getExpire());
			response = null;
			break;
		case Constants.HEART_BEAT:
			// 心跳
			response = null;
			break;
		default:
			throw new WrongMethodException();
		}
		return response;
	}

	public BitCask getBitcask() {
		return bitcask;
	}

	public void setBitcask(BitCask bitcask) {
		this.bitcask = bitcask;
	}

	public Synchronizer getSynchronizer() {
		return synchronizer;
	}

	public void setSynchronizer(Synchronizer synchronizer) {
		this.synchronizer = synchronizer;
	}

}

package com.magic.gateway.server.dispatcher;

import com.magic.constants.Constants;
import com.magic.gateway.impl.Client;
import com.magic.gateway.server.netty.request.GatewayRequest;
import com.magic.gateway.server.netty.request.GatewayResponse;

public class GatewayDispatcherImpl implements GatewayDispatcher {

	private Client client;

	public GatewayDispatcherImpl(Client client) {
		this.setClient(client);
	}

	@Override
	public GatewayResponse getResult(GatewayRequest request) throws Exception {
		String method = request.getMethod().toLowerCase();
		GatewayResponse response = new GatewayResponse(request.getGuid());
		if (request.getExpire() < 0 && request.getExpire() != Constants.EXPIRE_TIME_DEFAULT) {
			response.setError(GatewayResponse.EXPIRE_ERROR);
			return response;
		}
		switch (method) {
		case Constants.GET:
			String value = client.get(request.getKey());
			if (value == null) {
				response.setError(GatewayResponse.NOT_EXIST_ERROR);
			} else {
				response.setValue(value);
			}
			break;
		case Constants.SET:
			if (request.getKey() == null) {
				response.setError(GatewayResponse.KEY_ERROR);
				return response;
			}
			if (request.getValue() == null) {
				response.setError(GatewayResponse.VALUE_ERROR);
				return response;
			}
			client.setWithExpire(request.getKey(), request.getValue(), request.getExpire());
			break;
		case Constants.DELETE:
			client.delete(request.getKey());
			break;
		case Constants.HEART_BEAT:
			// 心跳
			response = null;
			break;
		default:
			response.setError(GatewayResponse.METHOD_ERROR);
			return response;
		}
		return response;
	}

	public Client getClient() {
		return client;
	}

	public void setClient(Client client) {
		this.client = client;
	}

}

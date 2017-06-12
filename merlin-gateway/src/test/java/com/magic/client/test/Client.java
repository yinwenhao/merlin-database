package com.magic.client.test;

import java.util.concurrent.TimeUnit;

import com.magic.constants.Constants;
import com.magic.gateway.exception.TimeoutException;
import com.magic.gateway.server.netty.request.GatewayRequest;
import com.magic.gateway.server.netty.request.GatewayResponse;

public class Client {

	private Sender sender;

	public Client(Sender sender) {
		this.sender = sender;
	}

	public String get(String key) throws Exception {
		GatewayRequest request = new GatewayRequest(Constants.GET, key);
		GatewayResponse response = sender.sendRequest(request, 15, TimeUnit.SECONDS);
		if (response == null) {
			throw new TimeoutException();
		}
		if (response.getError() != 0) {
			throw new Exception("response error:"+response.getError());
		}
		return response.getValue();
	}

	public void set(String key, String value) throws Exception {
		setWithExpire(key, value, Constants.EXPIRE_TIME_DEFAULT);
	}

	public String setWithExpire(String key, String value, long expire) throws Exception {
		GatewayRequest request = new GatewayRequest(Constants.SET, key, value, expire);
		GatewayResponse response = sender.sendRequest(request, 15, TimeUnit.SECONDS);
		if (response == null) {
			throw new TimeoutException();
		}
		if (response.getError() != 0) {
			throw new Exception("response error:"+response.getError());
		}
		return response.getValue();
	}

	public String delete(String key) throws Exception {
		GatewayRequest request = new GatewayRequest(Constants.DELETE, key);
		GatewayResponse response = sender.sendRequest(request, 15, TimeUnit.SECONDS);
		if (response == null) {
			throw new TimeoutException();
		}
		if (response.getError() != 0) {
			throw new Exception("response error:"+response.getError());
		}
		return response.getValue();
	}

	public Sender getSender() {
		return sender;
	}

	public void setSender(Sender sender) {
		this.sender = sender;
	}
}

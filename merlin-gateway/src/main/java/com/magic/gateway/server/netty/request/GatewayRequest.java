package com.magic.gateway.server.netty.request;

import java.util.UUID;

import com.magic.constants.Constants;

public class GatewayRequest {

	private String guid;

	private long expire;

	private String method;

	private String key;

	private String value;

	public GatewayRequest() {
	}

	public GatewayRequest(String method) {
		this(method, "");
	}

	public GatewayRequest(String method, String key) {
		this(method, key, "");
	}

	public GatewayRequest(String method, String key, String value) {
		this(method, key, value, Constants.EXPIRE_TIME_DEFAULT);
	}

	public GatewayRequest(String method, String key, String value, long expire) {
		this.expire = expire;
		this.key = key;
		this.value = value;
		this.method = method;
		this.guid = UUID.randomUUID().toString();
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public long getExpire() {
		return expire;
	}

	public void setExpire(long expire) {
		this.expire = expire;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

}

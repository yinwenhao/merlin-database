package com.magic.gateway.server.netty.request;

public class GatewayResponse {

	public static final int OK = 0;
	public static final int NOT_EXIST_ERROR = 10;
	public static final int EXPIRE_ERROR = 20;
	public static final int METHOD_ERROR = 30;
	public static final int KEY_ERROR = 40;
	public static final int VALUE_ERROR = 50;

	private String guid;

	private int error;

	private String value;

	public GatewayResponse() {
	}

	public GatewayResponse(String guid) {
		this.guid = guid;
		this.error = OK;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public int getError() {
		return error;
	}

	public void setError(int error) {
		this.error = error;
	}

}

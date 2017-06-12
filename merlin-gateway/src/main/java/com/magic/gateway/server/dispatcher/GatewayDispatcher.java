package com.magic.gateway.server.dispatcher;

import com.magic.gateway.server.netty.request.GatewayRequest;
import com.magic.gateway.server.netty.request.GatewayResponse;

public interface GatewayDispatcher {

	public GatewayResponse getResult(GatewayRequest request) throws Exception;

}

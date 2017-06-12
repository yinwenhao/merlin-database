package com.magic.gateway.sender;

import java.util.Map;
import java.util.WeakHashMap;

public class SenderManager {

	public static Map<String, ShardResponseFuture> futureMap = new WeakHashMap<String, ShardResponseFuture>();

	public static synchronized void ensureFutureInMap(String guid, ShardResponseFuture future) {
		if (!futureMap.containsKey(guid)) {
			// 创建一个ShardResponseFuture
			futureMap.put(guid, future);
		}
	}

	public static ShardResponseFuture getShardResponseFuture(String guid) {
		return futureMap.get(guid);
	}

	public static void removeFuture(String guid) {
		futureMap.remove(guid);
	}

}

package com.magic.gateway;

public interface ConsistentHash {

	public int getShardIndex(String key, int shardNum);

}

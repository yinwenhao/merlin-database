package com.magic.gateway.impl;

import com.magic.gateway.ConsistentHash;
import com.magic.util.CRC32;

public class ConsistentHashImpl implements ConsistentHash {

	@Override
	public int getShardIndex(String key, int shardNum) {
		CRC32 crc = new CRC32();
		crc.update(key.getBytes());
		return crc.getValue() % shardNum;
	}

}

package com.magic.synchronize.merkletree;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.magic.util.CRC32;

public class MerkleTreeLeaf extends MerkleTreeNodeBase {

	private Map<String, Integer> keys;

	public MerkleTreeLeaf(int hash, String key) {
		super();
		this.keys = new TreeMap<String, Integer>();
		setHash(hash);
		this.keys.put(key, hash);
		CRC32 crc32 = new CRC32();
		crc32.update(key);
		setKeyCrc32(crc32.getValue());
	}

	public Set<String> getKeys() {
		return keys.keySet();
	}

	/**
	 * 尝试增加一个key
	 * 
	 * @param key
	 * @return true：增加了一个新的key或hash值变化了，重新计算hash false：hash值没变
	 */
	public boolean checkAddKey(String key, int hash) {
		if (keys.containsKey(key) && keys.get(key).equals(hash)) {
			return false;
		}
		keys.put(key, hash);
		CRC32 crc32 = new CRC32();
		for (Entry<String, Integer> en : keys.entrySet()) {
			crc32.update(en.getValue());
		}
		setHash(crc32.getValue());
		return true;
	}

}

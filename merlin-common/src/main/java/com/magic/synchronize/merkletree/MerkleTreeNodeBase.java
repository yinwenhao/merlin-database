package com.magic.synchronize.merkletree;

public class MerkleTreeNodeBase {

	private int hash; // 根据子节点的hash生成的hash，或者自己的crc32

	private int keyCrc32; // key的crc32值，小于这个值的在leftSon，大于等于这个值得在rightSon

	public int getHash() {
		return hash;
	}

	public void setHash(int hash) {
		this.hash = hash;
	}

	public int getKeyCrc32() {
		return keyCrc32;
	}

	public void setKeyCrc32(int keyCrc32) {
		this.keyCrc32 = keyCrc32;
	}

}

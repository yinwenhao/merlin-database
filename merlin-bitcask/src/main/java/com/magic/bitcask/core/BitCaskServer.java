package com.magic.bitcask.core;

import java.io.IOException;

import com.magic.bitcask.entity.BitCaskValue;
import com.magic.synchronize.merkletree.MerkleTreeNode;

public interface BitCaskServer extends BitCask {

	public MerkleTreeNode getMerkleTreeRoot();

	public BitCaskValue getRealValue(String key) throws IOException;

	public void close() throws IOException;

	public void mergeAll() throws Exception;

	public void scheduleMerge() throws Exception;

}

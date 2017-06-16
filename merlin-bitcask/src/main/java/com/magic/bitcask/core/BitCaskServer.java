package com.magic.bitcask.core;

import java.io.IOException;

import com.magic.bitcask.entity.BitCaskValue;
import com.magic.exception.BaseException;
import com.magic.synchronize.merkletree.MerkleTreeNode;

public interface BitCaskServer extends BitCask {

	public MerkleTreeNode getMerkleTreeRoot();

	/**
	 * 内部接口，不检查是否只读
	 * 
	 * @param key
	 * @param value
	 * @param version
	 * @param expire
	 * @throws IOException
	 * @throws BaseException
	 */
	public void innerSetWithExpire(String key, String value, long version, long expire)
			throws IOException, BaseException;

	public BitCaskValue getRealValue(String key) throws IOException;

	public void close() throws IOException;

	public void mergeAll() throws Exception;

	public void scheduleMerge() throws Exception;

}

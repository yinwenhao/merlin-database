package com.magic.bitcask.core;

import java.io.IOException;

import com.magic.bitcask.entity.BitCaskValue;
import com.magic.bitcask.exception.BaseException;

public interface BitCask {

	public void set(String key, String value, long version) throws IOException, BaseException;

	public void setWithExpire(String key, String value, long version, long expire) throws IOException, BaseException;

	public BitCaskValue get(String key) throws IOException;

	public void delete(String key, long version) throws IOException, BaseException;

}

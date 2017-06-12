package com.magic.gateway;

import com.magic.gateway.exception.BaseException;

public interface MagicClient {

	/**
	 * 获得一个值
	 * 
	 * @param key
	 * @return
	 * @throws BaseException
	 * @throws InterruptedException
	 */
	public String get(String key) throws BaseException, InterruptedException;

	/**
	 * 设置一个值
	 * 
	 * @param key
	 * @param value
	 * @throws BaseException
	 * @throws InterruptedException
	 */
	public void set(String key, String value) throws BaseException, InterruptedException;

	/**
	 * 设置一个值，带过期时间的
	 * 
	 * @param key
	 * @param value
	 * @param expire
	 *            有效期毫秒数
	 * @throws BaseException
	 * @throws InterruptedException
	 */
	public void setWithExpire(String key, String value, long expire) throws BaseException, InterruptedException;

	/**
	 * 删除一个值
	 * 
	 * @param key
	 * @throws BaseException
	 * @throws InterruptedException
	 */
	public void delete(String key) throws BaseException, InterruptedException;

	/**
	 * 关闭
	 */
	public void close();

}

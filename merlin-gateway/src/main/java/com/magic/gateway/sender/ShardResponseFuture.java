package com.magic.gateway.sender;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.magic.netty.request.Response;

public class ShardResponseFuture implements Future<Response> {

	/**
	 * future类型
	 * 
	 * @author yinwenhao
	 *
	 */
	private enum Mode {

		/**
		 * 读取模式——读R个节点
		 */
		NORMAL_READ,

		/**
		 * 读取模式——读R个相同的值，可能需要读所有节点
		 */
		SAME_VALUE_READ;
	}

	// 因为请求和响应是一一对应的，因此初始化CountDownLatch值为1。
	private CountDownLatch latch;

	// 需要响应线程设置的响应结果
	private Response response;

	// 每个实例的返回结果<结果，个数>
	private Map<Response, Integer> oneResponseMap;

	// 收到的返回结果个数
	private int index;

	// 需要返回的个数
	private int num;

	// 需要相同结果的个数
	private int checkNum;

	/**
	 * 模式：0读R个节点，取最新的结果；1读R个相同的值，可能需要读所有节点。</br>
	 * 默认0
	 * 
	 * @see com.magic.gateway.sender.ShardResponseFuture.Mode
	 */
	private Mode mode;

	// Futrue的请求时间，用于计算Future是否超时
	private long beginTime;

	/**
	 * 取所有返回中version最新的值
	 * 
	 * @param num
	 *            需要返回的个数
	 */
	public ShardResponseFuture(int num) {
		this(num, num, Mode.NORMAL_READ);
	}

	/**
	 * 需要有checkNum个返回相同的值
	 * 
	 * @param num
	 *            需要返回的个数
	 * @param checkNum
	 *            需要相同结果的个数
	 */
	public ShardResponseFuture(int num, int checkNum) {
		this(num, checkNum, Mode.SAME_VALUE_READ);
	}

	private ShardResponseFuture(int num, int checkNum, Mode mode) {
		this.latch = new CountDownLatch(1);
		this.beginTime = System.currentTimeMillis();
		this.num = num;
		this.checkNum = checkNum;
		this.oneResponseMap = new HashMap<Response, Integer>(num);
		this.index = 0;
		this.mode = mode;
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return false;
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public boolean isDone() {
		if (response != null) {
			return true;
		}
		return false;
	}

	// 获取响应结果，直到有结果才返回。
	@Override
	public Response get() throws InterruptedException {
		latch.await();
		return getResponse();
	}

	// 获取响应结果，直到有结果或者超过指定时间就返回。
	@Override
	public Response get(long timeout, TimeUnit unit) throws InterruptedException {
		latch.await(timeout, unit);
		return getResponse();
	}

	public synchronized void setOneResponse(Response response) {
		if (oneResponseMap.containsKey(response)) {
			oneResponseMap.put(response, oneResponseMap.get(response) + 1);
		} else {
			oneResponseMap.put(response, 1);
		}
		index++;
		if (index >= num) {
			// 收到超过规定数量的返回结果
			switch (mode) {
			case SAME_VALUE_READ:
				for (Entry<Response, Integer> en : oneResponseMap.entrySet()) {
					if (en.getValue() >= checkNum) {
						// 超过规定数量的返回结果相同，设置为结果
						setResponse(en.getKey());
						return;
					}
				}
				break;
			case NORMAL_READ:
				// 默认为NORMAL_READ模式，取最新的值设置为结果
			default:
				// 默认为NORMAL_READ模式，取最新的值设置为结果
				Response r = null;
				for (Entry<Response, Integer> en : oneResponseMap.entrySet()) {
					if (r == null || r.getVersion() < en.getKey().getVersion()) {
						r = en.getKey();
					}
				}
				setResponse(r);
				break;
			}
		}
	}

	// 用于设置响应结果，并且做countDown操作，通知请求线程
	private void setResponse(Response response) {
		this.response = response;
		latch.countDown();
	}

	private Response getResponse() {
		return this.response;
	}

	public long getBeginTime() {
		return beginTime;
	}

	public int getCheckNum() {
		return checkNum;
	}

	public void setCheckNum(int checkNum) {
		this.checkNum = checkNum;
	}
}

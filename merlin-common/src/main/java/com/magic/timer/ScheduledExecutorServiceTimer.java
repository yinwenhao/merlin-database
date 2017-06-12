package com.magic.timer;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScheduledExecutorServiceTimer implements Timer {

	private final Logger log = LoggerFactory.getLogger(getClass());

	/**
	 * 等待线程池关闭的时间（秒）
	 */
	private static final long AWAIT_SECONDS = 15;

	private ScheduledExecutorService ses;

	private ScheduledExecutorServiceTimer() {
		ses = Executors.newSingleThreadScheduledExecutor();
	}

	/**
	 * 使用SingletonHolder实现 lazy-loaded singleton.
	 * 
	 * @author yinwenhao
	 *
	 */
	private static class SingletonHolder {
		private static ScheduledExecutorServiceTimer instance = new ScheduledExecutorServiceTimer();
	}

	public static Timer getInstance() {
		return SingletonHolder.instance;
	}

	@Override
	public TimerFuture scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
		ScheduledFuture<?> future = ses.scheduleAtFixedRate(command, initialDelay, period, unit);
		return new ScheduledExecutorServiceTimerFuture(future);
	}

	@Override
	public TimerFuture schedule(Runnable command, long delay, TimeUnit unit) {
		ScheduledFuture<?> future = ses.schedule(command, delay, unit);
		return new ScheduledExecutorServiceTimerFuture(future);
	}

	@Override
	public TimerFuture scheduleAtFixedRate(Runnable command, Date startTime, long period, TimeUnit unit) {
		ScheduledFuture<?> future = ses.scheduleAtFixedRate(command, startTime.getTime() - System.currentTimeMillis(),
				TimeUnit.MILLISECONDS.convert(period, unit), TimeUnit.MILLISECONDS);
		return new ScheduledExecutorServiceTimerFuture(future);
	}

	@Override
	public void shutdown() throws Exception {
		ses.shutdown();
		if (!ses.awaitTermination(AWAIT_SECONDS, TimeUnit.SECONDS)) {
			log.warn("ses: close threads... ({}) take too long time: {}s.", new Date(), AWAIT_SECONDS);
		}
	}

}

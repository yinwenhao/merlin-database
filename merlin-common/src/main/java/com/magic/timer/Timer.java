package com.magic.timer;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public interface Timer {

	public TimerFuture scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit);

	public TimerFuture scheduleAtFixedRate(Runnable command, Date startTime, long period, TimeUnit unit);

	public void shutdown() throws Exception;

	TimerFuture schedule(Runnable command, long delay, TimeUnit unit);

}

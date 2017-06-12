package com.magic.timer;

import java.util.concurrent.ScheduledFuture;

public class ScheduledExecutorServiceTimerFuture implements TimerFuture {

	private ScheduledFuture<?> future;

	public ScheduledExecutorServiceTimerFuture(ScheduledFuture<?> future) {
		this.future = future;
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return future.cancel(mayInterruptIfRunning);
	}

}

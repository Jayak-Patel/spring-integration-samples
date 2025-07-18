package org.springframework.integration.sts;

import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;

public class MessagePublishingRetryListener implements RetryListener {

	@Override
	public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
		System.out.println(">>>>>OPEN<<<<<");
		return true;
	}

	@Override
	public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
		System.out.println(">>>>>CLOSE<<<<<");
	}

	@Override
	public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
		System.out.println(">>>>>ON ERROR: attempt " + context.getRetryCount() + "<<<<<");
	}

}

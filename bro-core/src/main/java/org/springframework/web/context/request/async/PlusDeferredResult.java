package org.springframework.web.context.request.async;


public class PlusDeferredResult<T> extends DeferredResult<T> {
	private Runnable retrieveMessageCallback;

	public Runnable getRetrieveMessageCallback() {
		return retrieveMessageCallback;
	}

	public void setRetrieveMessageCallback(Runnable retrieveMessageCallback) {
		this.retrieveMessageCallback = retrieveMessageCallback;
	}
}

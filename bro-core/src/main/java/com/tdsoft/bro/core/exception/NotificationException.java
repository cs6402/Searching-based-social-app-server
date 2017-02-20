package com.tdsoft.bro.core.exception;

import com.tdsoft.bro.core.exception.code.NotificationErrorCode;

public class NotificationException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	NotificationErrorCode errorCode;

	public NotificationException(String errorMessage, NotificationErrorCode errorCode, Exception cause) {
		super(errorMessage, cause);
		this.errorCode = errorCode;
	}

}

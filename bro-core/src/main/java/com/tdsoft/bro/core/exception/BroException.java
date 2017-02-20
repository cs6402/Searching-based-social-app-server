package com.tdsoft.bro.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 系統最上層自定義例外，其他例外繼承此類別
 * @author Daniel
 *
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class BroException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public BroException() {
		super();
	}

	public BroException(String message) {
		super(message);
	}

	public BroException(String message, Throwable cause) {
		super(message, cause);
	}

	public BroException(Throwable cause) {
		super(cause);
	}

}

package com.tdsoft.bro.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 傳入參數錯誤時，統一採用此類別(因為需要返回狀態碼，故不使用IllegalArgumentException)
 * @author Daniel
 *
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ArgumentInvalidException extends BroException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ArgumentInvalidException() {
		super();
	}

	public ArgumentInvalidException(String message) {
		super(message);
	}

	public ArgumentInvalidException(String message, Throwable cause) {
		super(message, cause);
	}

	public ArgumentInvalidException(Throwable cause) {
		super(cause);
	}

}

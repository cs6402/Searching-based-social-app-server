package com.tdsoft.bro.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 裝置代號認證失敗
 * @author Daniel
 *
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class DeviceNotFoundException extends BroException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DeviceNotFoundException() {
		super();
	}

	public DeviceNotFoundException(String message) {
		super(message);
	}

	public DeviceNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public DeviceNotFoundException(Throwable cause) {
		super(cause);
	}
}

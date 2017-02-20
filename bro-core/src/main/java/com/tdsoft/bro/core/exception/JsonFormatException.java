package com.tdsoft.bro.core.exception;

/**
 * Json格式轉換錯誤
 * @author Daniel
 *
 */
public class JsonFormatException extends BroException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public JsonFormatException() {
		super();
	}

	public JsonFormatException(String message) {
		super(message);
	}

	public JsonFormatException(String message, Throwable cause) {
		super(message, cause);
	}

	public JsonFormatException(Throwable cause) {
		super(cause);
	}
}

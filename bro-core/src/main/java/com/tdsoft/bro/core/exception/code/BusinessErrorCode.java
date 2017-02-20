package com.tdsoft.bro.core.exception.code;

public enum BusinessErrorCode implements ErrorCode {
	B0001("Unknown Business Error"), B0101("Invalid Argument"), B0102("Invalid Method"), B0201("Device Error"), B0301("Message Error");
	String message;

	private BusinessErrorCode(String message) {
		this.message = message;
	}

	@Override
	public String getCauseMessage() {
		return message;
	}

	@Override
	public String getCode() {
		return toString();
	}

}

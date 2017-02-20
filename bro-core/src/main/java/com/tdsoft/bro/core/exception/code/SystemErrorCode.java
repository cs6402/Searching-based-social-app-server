package com.tdsoft.bro.core.exception.code;

public enum SystemErrorCode implements ErrorCode {
	S0001("Unknown"), S0002("Amazon Unknown error");
	
	private SystemErrorCode(String message) {
		this.message = message;
	}

	String message;

	@Override
	public String getCauseMessage() {
		return message;
	}

	@Override
	public String getCode() {
		return this.toString();
	}
}

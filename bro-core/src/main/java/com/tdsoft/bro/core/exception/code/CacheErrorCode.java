package com.tdsoft.bro.core.exception.code;

public enum CacheErrorCode implements ErrorCode {
	S0201("Cache Service Error"), S0202("Cache Service Connection Refused"), S0203("Cache Service Connection Timeout"), S0204(
			"Could not find data");

	String message;

	private CacheErrorCode(String message) {
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

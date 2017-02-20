package com.tdsoft.bro.core.exception.code;

public enum QueueErrorCode implements ErrorCode {
	S0301("Queue Service Unknown Error"), S0302("Queue Service Connection Refused"), S0303("Invalid Arguments"), S0304("Invalid Batch Arguments");
	

	String message;

	private QueueErrorCode(String message) {
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

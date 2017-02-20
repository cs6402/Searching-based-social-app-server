package com.tdsoft.bro.core.exception.code;

public enum NotificationErrorCode implements ErrorCode {
	S0401("Notification Service Unknown Error"), S0402("Notification Service Connection Refused"), S0403("Could not find push target"), S0404("Invalid Argument");

	String message;

	private NotificationErrorCode(String message) {
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

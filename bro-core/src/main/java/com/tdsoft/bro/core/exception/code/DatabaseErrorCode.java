package com.tdsoft.bro.core.exception.code;

public enum DatabaseErrorCode implements ErrorCode {
	S0101("Database Unkown Error"), S0102("Database Connection Refused"), S0103("Database Connection Timeout"), S0104("Duplicate Key"),
	S0105("Constraint Violation Error");

	String message;

	private DatabaseErrorCode(String message) {
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

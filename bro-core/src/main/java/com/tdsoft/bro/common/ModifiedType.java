package com.tdsoft.bro.common;

public enum ModifiedType {
	ADD("add"), UPADTE("update");
	String abbreviation;

	private ModifiedType(String abbreviation) {
		this.abbreviation = abbreviation;
	}

	@Override
	public String toString() {
		return abbreviation;
	}
}

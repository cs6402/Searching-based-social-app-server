package com.tdsoft.bro.core.bean;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TagCSSearchBean extends TagSearchBean {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@JsonProperty("LOC")
	private String location;

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

}

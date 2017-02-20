package com.tdsoft.bro.core.bean;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TagESSearchBean extends TagSearchBean {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@JsonProperty("LOC")
	private Map<String, Double> location;

	public Map<String, Double> getLocation() {
		if (location == null) {
			location = new HashMap<String, Double>();
		}
		return location;
	}

	public void setLocation(Map<String, Double> location) {
		this.location = location;
	}
}

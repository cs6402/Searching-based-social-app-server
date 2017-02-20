package com.tdsoft.bro.core.bean;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class TagCacheBean implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@JsonProperty("I")
	private String id;

	@JsonProperty("F")
	@JsonDeserialize(as = TagESSearchBean.class)
	private TagSearchBean field;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public TagSearchBean getField() {
		return field;
	}

	public void setField(TagSearchBean field) {
		this.field = field;
	}
}

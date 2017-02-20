package com.tdsoft.bro.core.bean;

import com.amazonaws.services.cloudsearchdomain.model.ContentType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tdsoft.bro.common.ModifiedType;

public class TagCSCacheBean extends TagCacheBean {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@JsonProperty("id")
	private String id;
	@JsonProperty("fields")
	private TagSearchBean field;
	@JsonProperty("type")
	private String type = ModifiedType.ADD.toString();
	@JsonProperty("content_encoding")
	private String contentEncoding = "UTF8";
	@JsonProperty("content_type")
	private String contentType = ContentType.Applicationjson.toString();
	
	@Override
	public String getId() {
		return id;
	}
	@Override
	public void setId(String id) {
		this.id = id;
	}
	@Override
	public TagSearchBean getField() {
		return field;
	}
	@Override
	public void setField(TagSearchBean field) {
		this.field = field;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getContentEncoding() {
		return contentEncoding;
	}

	public void setContentEncoding(String contentEncoding) {
		this.contentEncoding = contentEncoding;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

}

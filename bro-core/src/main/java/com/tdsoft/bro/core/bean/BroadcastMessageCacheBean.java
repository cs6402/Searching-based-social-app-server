package com.tdsoft.bro.core.bean;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BroadcastMessageCacheBean implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@JsonProperty("C")
	private String content;
	@JsonProperty("CR")
	private Long timestamp;

	/**
	 * 廣播訊息內容，Json格式，內容為BroadcastMessageDTO
	 * 
	 * @return 廣播訊息內容
	 */
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * 發送時間 e.g:1441901795512
	 * 
	 * @return 發送時間
	 */
	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}
}

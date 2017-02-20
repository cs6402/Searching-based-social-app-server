package com.tdsoft.bro.dispatcher.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageSuccessDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@JsonProperty(value = "I", required = true)
	private String messageId;
	@JsonProperty(value = "CR", required = true)
	private Long timestamp;

	/**
	 * 伺服器端訊息代號，主要為SQS訊息代號	e.g:d589ca57-3214-406b-b7f2-388932776eaa
	 * @return 伺服器端訊息代號
	 */
	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	/**
	 * 伺服器處理完成時間，epoch格式，GMT+8時區 	e.g:1441901795512
	 * @return 伺服器處理完成時間
	 */
	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}
}

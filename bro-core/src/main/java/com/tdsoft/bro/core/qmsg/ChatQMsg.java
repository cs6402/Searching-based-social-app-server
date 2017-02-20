package com.tdsoft.bro.core.qmsg;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChatQMsg implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@JsonProperty("C")
	private String content;

	@JsonProperty("I")
	private String senderDeviceId;

	@JsonProperty("R")
	private String receiverDeviceId;
	
	@JsonProperty("CI")
	private String clientMessageId;
	
	@JsonProperty("CR")
	private Long timestamp;

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getSenderDeviceId() {
		return senderDeviceId;
	}

	public void setSenderDeviceId(String senderDeviceId) {
		this.senderDeviceId = senderDeviceId;
	}

	public String getReceiverDeviceId() {
		return receiverDeviceId;
	}

	public void setReceiverDeviceId(String receiverDeviceId) {
		this.receiverDeviceId = receiverDeviceId;
	}

	public String getClientMessageId() {
		return clientMessageId;
	}

	public void setClientMessageId(String clientMessageId) {
		this.clientMessageId = clientMessageId;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

}

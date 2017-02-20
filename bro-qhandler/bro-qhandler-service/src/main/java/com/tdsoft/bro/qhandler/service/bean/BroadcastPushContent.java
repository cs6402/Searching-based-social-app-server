package com.tdsoft.bro.qhandler.service.bean;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tdsoft.bro.common.MessageType;

public class BroadcastPushContent implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@JsonProperty("T")
	private String title;

	@JsonProperty("C")
	private String content;

	@JsonProperty("MT")
	private MessageType messageType;
	
	@JsonProperty("LON")
	private double longtitude;
	
	@JsonProperty("LAT")
	private double latitude;
	
	@JsonProperty("I")
	private String deviceId;
	
	@JsonProperty("CI")
	private String clientMessageId;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public MessageType getMessageType() {
		return messageType;
	}

	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}

	public double getLongtitude() {
		return longtitude;
	}

	public void setLongtitude(double longtitude) {
		this.longtitude = longtitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}


	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getClientMessageId() {
		return clientMessageId;
	}

	public void setClientMessageId(String clientMessageId) {
		this.clientMessageId = clientMessageId;
	}
}

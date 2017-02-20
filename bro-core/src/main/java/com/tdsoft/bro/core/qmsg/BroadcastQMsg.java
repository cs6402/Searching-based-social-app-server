package com.tdsoft.bro.core.qmsg;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tdsoft.bro.common.MessageType;

public class BroadcastQMsg implements Serializable {

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

	@JsonProperty("R")
	private int radius;

	@JsonProperty("I")
	private String deviceId;

	// TODO 以下與文件不同，此為特定用於某些方法
	@JsonProperty("N")
	private Long deviceNo;

	@JsonProperty("CR")
	private Long createtime;

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

	public Long getCreatetime() {
		return createtime;
	}

	public void setCreatetime(Long createtime) {
		this.createtime = createtime;
	}

	public MessageType getMessageType() {
		return messageType;
	}

	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongtitude() {
		return longtitude;
	}

	public void setLongtitude(double longtitude) {
		this.longtitude = longtitude;
	}

	public int getRadius() {
		return radius;
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public Long getDeviceNo() {
		return deviceNo;
	}

	public void setDeviceNo(Long deviceNo) {
		this.deviceNo = deviceNo;
	}


}

package com.tdsoft.bro.core.qmsg;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tdsoft.bro.common.DeviceType;

public class UploadTagQMsg implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@JsonProperty("I")
	private String deviceId;
	@JsonProperty("TS")
	private String[] tags;
	@JsonProperty("LON")
	private double longtitude;
	@JsonProperty("LAT")
	private double latitude;
	@JsonProperty("N")
	private Long deviceNo;
	@JsonProperty("S")
	private String snsToken;
	@JsonProperty("T")
	private DeviceType deviceType;
	@JsonProperty("A")
	private String aliasName;
	@JsonProperty(value = "IMG")
	private String image;
	
	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String[] getTags() {
		return tags;
	}

	public void setTags(String[] tags) {
		this.tags = tags;
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

	public Long getDeviceNo() {
		return deviceNo;
	}

	public void setDeviceNo(Long deviceNo) {
		this.deviceNo = deviceNo;
	}

	public String getSnsToken() {
		return snsToken;
	}

	public void setSnsToken(String snsToken) {
		this.snsToken = snsToken;
	}

	public DeviceType getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(DeviceType deviceType) {
		this.deviceType = deviceType;
	}

	public String getAliasName() {
		return aliasName;
	}

	public void setAliasName(String aliasName) {
		this.aliasName = aliasName;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}
}

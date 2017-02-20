package com.tdsoft.bro.core.qmsg;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tdsoft.bro.common.DeviceType;

public class UpdateLocationQMsg implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@JsonProperty("I")
	private String deviceId;

	@JsonProperty("Lon")
	private double longtitude;
	@JsonProperty("Lat")
	private double latitude;

	@JsonProperty("T")
	private DeviceType deviceType;

	// TODO 以下與文件不同，此為特定用於某些方法
	@JsonProperty("S")
	private String snsToken;
	@JsonProperty("N")
	private Long deviceNo;
	@JsonProperty("O_Lon")
	private double oldLongtitude;
	@JsonProperty("O_Lat")
	private double oldLatitude;

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

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
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

	public Long getDeviceNo() {
		return deviceNo;
	}

	public void setDeviceNo(Long deviceNo) {
		this.deviceNo = deviceNo;
	}

	public double getOldLongtitude() {
		return oldLongtitude;
	}

	public void setOldLongtitude(double oldLongtitude) {
		this.oldLongtitude = oldLongtitude;
	}

	public double getOldLatitude() {
		return oldLatitude;
	}

	public void setOldLatitude(double oldLatitude) {
		this.oldLatitude = oldLatitude;
	}


}

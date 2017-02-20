package com.tdsoft.bro.core.qmsg;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tdsoft.bro.core.entity.DeviceInfoEntity;

public class InsertDeviceQMsg implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@JsonProperty("D")
	private DeviceInfoEntity entity;
	@JsonProperty("LON")
	private double longtitude;
	@JsonProperty("LAT")
	private double latitude;

	public DeviceInfoEntity getEntity() {
		return entity;
	}

	public void setEntity(DeviceInfoEntity entity) {
		this.entity = entity;
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

}

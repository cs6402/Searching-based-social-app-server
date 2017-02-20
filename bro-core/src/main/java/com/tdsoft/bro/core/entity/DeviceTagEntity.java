package com.tdsoft.bro.core.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "device_tag")
public class DeviceTagEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@Column
	private Long deviceNo;
	@Column
	private String[] tags;
	@Column
	private double latitude;
	@Column
	private double longtitude;
	@Column
	private Date lastRepostTime;

	public Long getDeviceNo() {
		return deviceNo;
	}

	public void setDeviceNo(Long deviceNo) {
		this.deviceNo = deviceNo;
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

	public Date getLastRepostTime() {
		return lastRepostTime;
	}

	public void setLastRepostTime(Date lastRepostTime) {
		this.lastRepostTime = lastRepostTime;
	}
}

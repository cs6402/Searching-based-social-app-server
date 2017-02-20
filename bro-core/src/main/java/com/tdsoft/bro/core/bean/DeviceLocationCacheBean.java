package com.tdsoft.bro.core.bean;

import java.io.Serializable;

import com.tdsoft.bro.common.DeviceType;

public class DeviceLocationCacheBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Long point;
	
	private DeviceType deviceType;
	
	private String snsToken;

	public DeviceType getDeviceType() {
		return deviceType;
	}

	public void setDeviceType(DeviceType deviceType) {
		this.deviceType = deviceType;
	}

	public String getSnsToken() {
		return snsToken;
	}

	public void setSnsToken(String snsToken) {
		this.snsToken = snsToken;
	}

	public void readFromCache(String value) {
		// 先不用json格式做資料結構，單純使用+串接
		char type = value.charAt(0);
		String snsToken = value.substring(1);
		setDeviceType(DeviceType.values()[Character.getNumericValue(type)]);
		setSnsToken(snsToken);
	}
	
	public String writeToCache() {
		// 先不用json格式做資料結構，單純使用+串接
		int ordinal = getDeviceType().ordinal();
		String snsToken = getSnsToken();
		return ordinal + snsToken;
	}

	public Long getPoint() {
		return point;
	}

	public void setPoint(Long point) {
		this.point = point;
	}
	
}

package com.tdsoft.bro.core.bean;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tdsoft.bro.common.DeviceType;

/**
 * 
 * 存放於cache的DeviceInfo
 * 
 * @author Daniel
 * 
 */
public class DeviceInfoCacheBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 裝置序號，Json鍵值為N
	 */
	@JsonProperty("N")
	private Long deviceNo;
	/**
	 * SNSToken，Json鍵值為S
	 */
	@JsonProperty("S")
	private String snsToken;

	/**
	 * UUID，Json鍵值為U
	 */
	@JsonProperty("U")
	private String uuid;

	/**
	 * 裝置代號，Json鍵值為I
	 */
	@JsonProperty("I")
	private String deviceId;

	/**
	 * 裝置類型，Json鍵值為T
	 */
	@JsonProperty("T")
	private DeviceType type;

	@JsonProperty("LON")
	private double longtitude;

	@JsonProperty("LAT")
	private double latitude;
	
	@JsonProperty("A")
	private String aliasName;

	@JsonProperty("IMG")
	private String image;
	
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

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public DeviceType getType() {
		return type;
	}

	public void setType(DeviceType type) {
		this.type = type;
	}

	public Double getLongtitude() {
		return longtitude;
	}

	public void setLongtitude(double longtitude) {
		this.longtitude = longtitude;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
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

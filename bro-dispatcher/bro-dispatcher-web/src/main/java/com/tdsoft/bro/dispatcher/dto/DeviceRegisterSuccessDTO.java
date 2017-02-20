package com.tdsoft.bro.dispatcher.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DeviceRegisterSuccessDTO implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@JsonProperty(value = "I", required = true)
	private String deviceId;
	@JsonProperty(value = "S", required = true)
	private String snsToken;
	@JsonProperty(value = "A", required = true)
	private String aliasName;
	@JsonProperty(value = "IMG", required = true)
	private String image;

	/**
	 * 系統用裝置代號 e.g:1138819804752832943@B
	 * 
	 * @return 系統用裝置代號
	 */
	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	/**
	 * 目前為裝置第二認證資訊 e.g:d0247fcc-f1ac-31b8-9fa1-e5eeb4e3e01e
	 * 
	 * @return 目前為裝置第二認證資訊
	 */
	public String getSnsToken() {
		return snsToken;
	}

	public void setSnsToken(String snsToken) {
		this.snsToken = snsToken;
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

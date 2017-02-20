package com.tdsoft.bro.dispatcher.dto;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tdsoft.bro.common.DeviceType;

public class DeviceRefreshDTO implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@JsonProperty(value = "TK", required = true)
	@NotEmpty
	private String token;
	@JsonProperty(value = "T", required = true)
	@NotNull
	private DeviceType type;

	/**
	 * 更新的GCM、BAIDU、APNS推送代號、	e.g:1138819804752832943,3658436104846344528
	 * @return 更新的GCM、BAIDU、APNS推送代號、
	 */
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	/**
	 * 裝置類型	e.g:BAIDU
	 * @return 裝置類型
	 */
	public DeviceType getType() {
		return type;
	}

	public void setType(DeviceType type) {
		this.type = type;
	}

}

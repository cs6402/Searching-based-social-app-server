package com.tdsoft.bro.dispatcher.dto;

import java.io.Serializable;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tdsoft.bro.common.DeviceType;

public class DeviceRegisterDTO implements Serializable {
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

	@JsonProperty(value = "U", required = true)
	@NotEmpty
	private String uuid;

	@JsonProperty(value = "LON", required = true)
	@DecimalMax("180")
	@DecimalMin("-180")
	@NotNull
	private Double longtitude;

	@JsonProperty(value = "LAT", required = true)
	@DecimalMax("90")
	@DecimalMin("-90")
	@NotNull
	private Double latitude;
	
	@JsonProperty(value = "LAN", required = true)
	@NotEmpty
	private String lang;

	/**
	 * GCM、BAIDU、APNS推送代號、 e.g:1138819804752832943,3658436104846344528
	 * 
	 * @return GCM、BAIDU、APNS推送代號
	 */
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	/**
	 * 裝置類型 e.g:BAIDU
	 * 
	 * @return 裝置類型
	 */
	public DeviceType getType() {
		return type;
	}

	public void setType(DeviceType type) {
		this.type = type;
	}

	/**
	 * 裝置唯一代號，BAIDU以UserId為主，APNS則使用廣告商代號，GCM不管，若此處空白則系統生成 e.g:1138819804752832943
	 * 
	 * @return 裝置唯一代號
	 */
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	/**
	 * 經度 e.g:121.564970
	 * 
	 * @return 經度
	 */
	public double getLongtitude() {
		return longtitude;
	}

	public void setLongtitude(double longtitude) {
		this.longtitude = longtitude;
	}

	/**
	 * 緯度 e.g:25.061991
	 * 
	 * @return 緯度
	 */
	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

}

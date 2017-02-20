package com.tdsoft.bro.core.bean;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TagSearchResultBean implements Serializable {

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
	@JsonProperty("DS")
	private double distance;
	@JsonProperty("A")
	private String aliasName = "";
	@JsonProperty("IMG")
	private String image = "";

	/**
	 * 該Tag擁有者的裝置代號 e.g:1138819804752839813@B
	 * 
	 * @return 該Tag擁有者的裝置代號
	 */
	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	/**
	 * Tag內容，陣列格式，內容為[想吃牛排, 喜歡跑步, 日本旅遊中]
	 * 
	 * @return Tag內容
	 */
	public String[] getTags() {
		return tags;
	}

	public void setTags(String[] tags) {
		this.tags = tags;
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
	 * 距離 單位:公里 e.g:0.037374537038265
	 * 
	 * @return 距離
	 */
	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
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

package com.tdsoft.bro.dispatcher.dto;

import java.io.Serializable;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tdsoft.bro.common.MessageType;

public class BroadcastMessageDTO implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@JsonProperty(value = "T", required = true)
	@NotEmpty
	private String title;

	@JsonProperty(value = "C", required = true)
	@NotEmpty
	private String content;

	@JsonProperty(value = "MT", required = true)
	@NotNull
	private MessageType messageType;

	@JsonProperty(value = "LAT", required = true)
	@DecimalMax("90")
	@DecimalMin("-90")
	@NotNull
	private Double latitude;

	@JsonProperty(value = "LON", required = true)
	@DecimalMax("180")
	@DecimalMin("-180")
	@NotNull
	private Double longtitude;

	@JsonProperty(value = "R", required = true)
	@NotNull
	@DecimalMin("1")
	private Integer radius;

	/**
	 * 標題 e.g:新人自我介紹
	 * 
	 * @return 標題
	 */
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * 廣播內容 e.g:Hi 我是新加入的小迪克老舊
	 * 
	 * @return 廣播內容
	 */
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * 訊息類別 e.g:Broadcast
	 * 
	 * @return 訊息類別
	 */
	public MessageType getMessageType() {
		return messageType;
	}

	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
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
	 * 距離 e.g:1
	 * 
	 * @return 距離
	 */
	public int getRadius() {
		return radius;
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}
}

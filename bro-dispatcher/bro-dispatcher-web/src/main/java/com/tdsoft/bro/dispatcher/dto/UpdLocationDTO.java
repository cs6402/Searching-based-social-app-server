package com.tdsoft.bro.dispatcher.dto;

import java.io.Serializable;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdLocationDTO implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
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

	/**
	 * 經度 e.g:121.564970
	 * 
	 * @return 經度
	 */
	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	/**
	 * 緯度 e.g:25.061991
	 * 
	 * @return 緯度
	 */
	public double getLongtitude() {
		return longtitude;
	}

	public void setLongtitude(double longtitude) {
		this.longtitude = longtitude;
	}

}

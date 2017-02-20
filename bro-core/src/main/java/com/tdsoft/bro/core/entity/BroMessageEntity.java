package com.tdsoft.bro.core.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.tdsoft.bro.common.MessageType;

@Entity
@Table(name = "bro_message")
public class BroMessageEntity implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Id
	@Column
	@SequenceGenerator(name = "bro_msg_seq", sequenceName = "bro_msg_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bro_msg_seq")
	private Long id;
	@Column
	private String title;
	@Column
	private String content;
	@Column
	private Date createtime;
	@Column
	@Enumerated(EnumType.ORDINAL)
	private MessageType messageType;
	@Column
	private double latitude;
	@Column
	private double longtitude;
	@Column
	private int radius;
	@Column
	private Long deviceNo;
	@Column
	private String clientMessageId;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public MessageType getMessageType() {
		return messageType;
	}
	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
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
	public int getRadius() {
		return radius;
	}
	public void setRadius(int radius) {
		this.radius = radius;
	}
	public Long getDeviceNo() {
		return deviceNo;
	}
	public void setDeviceNo(Long deviceId) {
		this.deviceNo = deviceId;
	}
	public Date getCreatetime() {
		return createtime;
	}
	public void setCreatetime(Date createtime) {
		this.createtime = createtime;
	}
	public String getClientMessageId() {
		return clientMessageId;
	}
	public void setClientMessageId(String clientMessageId) {
		this.clientMessageId = clientMessageId;
	}
	
}

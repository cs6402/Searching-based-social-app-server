package com.tdsoft.bro.core.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import com.tdsoft.bro.common.DeviceType;

@Entity
@Table(name = "device_info")
public class DeviceInfoEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column
//	@SequenceGenerator(name = "device_seq", sequenceName = "device_seq", allocationSize = 1)
//	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "device_seq")
	@GeneratedValue
	private Long deviceNo;

	@Column
	private String token;

	@Column(updatable = false)
	@Enumerated(EnumType.ORDINAL)
	private DeviceType type;

	@Column
	private String snsToken;

	@Column
	private Date createtime;

	@Column(unique = true)
	private String deviceId;

	@Column
	private String uuid;
	@Column
	private String aliasName;
	@Column
	private String image;

	public Long getDeviceNo() {
		return deviceNo;
	}

	public void setDeviceNo(Long deviceNo) {
		this.deviceNo = deviceNo;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public DeviceType getType() {
		return type;
	}

	public void setType(DeviceType type) {
		this.type = type;
	}

	public Date getCreatetime() {
		return createtime;
	}

	public void setCreatetime(Date createtime) {
		this.createtime = createtime;
	}

	public String getSnsToken() {
		return snsToken;
	}

	public void setSnsToken(String snsToken) {
		this.snsToken = snsToken;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	@PrePersist
	protected void onCreate() {
		createtime = new Date();
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

package com.tdsoft.bro.dispatcher.dto;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChatMessageDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@JsonProperty(value = "C", required = true)
	@NotEmpty
	private String content;

	@JsonProperty(value = "I", required = true)
	@NotEmpty
	private String senderDeviceId;

	@JsonProperty(value = "R", required = true)
	@NotEmpty
	private String receiverDeviceId;

	@JsonProperty(value = "CI", required = true)
	@NotEmpty
	private String clientMessageId;

	/**
	 * 訊息內容 e.g:Hi 我是小迪克老舊
	 * 
	 * @return 訊息內容
	 */
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * 發送者裝置代號 e.g:1138819804752839813@B
	 * 
	 * @return 發送者裝置代號
	 */
	public String getSenderDeviceId() {
		return senderDeviceId;
	}

	public void setSenderDeviceId(String senderDeviceId) {
		this.senderDeviceId = senderDeviceId;
	}

	/**
	 * 接收者裝置代號 e.doaidm=doplremg/fewomfk@A
	 * 
	 * @return 接收者裝置代號
	 */
	public String getReceiverDeviceId() {
		return receiverDeviceId;
	}

	public void setReceiverDeviceId(String receiverDeviceId) {
		this.receiverDeviceId = receiverDeviceId;
	}

	/**
	 * 發送者端訊息代號(發送者當下的currentTimeMillis)，epoch格式，GMT+8時區，發送端可利用此代號查詢訊息是否處理成功，合理狀況下不可能同毫秒下有兩封訊息從裝置送出
	 * e.g:1441901795512
	 * 
	 * @return 發送者端訊息代號
	 */
	public String getClientMessageId() {
		return clientMessageId;
	}

	public void setClientMessageId(String clientMessageId) {
		this.clientMessageId = clientMessageId;
	}

}

package com.tdsoft.bro.msgcenter.dto;

import java.io.Serializable;
public class MessageStatusDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private boolean isSuccessful;
	
	/**
	 * 訊息是否處理完成	e.g:true
	 * @return 訊息是否處理完成
	 */
	public boolean isSuccessful() {
		return isSuccessful;
	}

	public void setSuccessful(boolean isSuccessful) {
		this.isSuccessful = isSuccessful;
	}
}

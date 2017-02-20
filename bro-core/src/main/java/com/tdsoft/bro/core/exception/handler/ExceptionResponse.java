package com.tdsoft.bro.core.exception.handler;

import java.io.Serializable;

public class ExceptionResponse implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String errorCode;
	private Object errorMessage;
	private String url;
	private String details;

	/**
	 * 系統類錯誤代碼，與HTTP不同，詳細需參照錯誤代碼文件 e.g:S0001
	 * 
	 * @return 系統類錯誤代碼
	 */
	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	/**
	 * 錯誤訊息 e.g:Error...
	 * 
	 * @return 錯誤訊息
	 */
	public Object getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(Object errorMessage) {
		this.errorMessage = errorMessage;
	}

	/**
	 * 錯誤產生的位址 e.g:http://.....
	 * 
	 * @return 錯誤產生的位址
	 */
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * 錯誤細節 e.g:xxxxx
	 * 
	 * @return 錯誤細節
	 */
	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}


}

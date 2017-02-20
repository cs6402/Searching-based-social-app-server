package com.tdsoft.bro.common;

public enum DeviceType {
	GCM("G"), APNS("A"), BAIDU("B");
	String abbreviation;

	private DeviceType(String abbreviation) {
		this.abbreviation = abbreviation;
	}

	@Override
	public String toString() {
		return abbreviation;
	}

	/**
	 * 從縮寫轉回裝置類型物件
	 * 
	 * @param abbreviation 裝置類型縮寫，G:GCM、A:APNS、B:BAIDU
	 * @return 裝置類型
	 * @throws IllegalArgumentException 傳入的縮寫不合法時
	 */
	public static DeviceType fromString(String abbreviation) throws IllegalArgumentException {
		switch (abbreviation) {
			case "G":
				return DeviceType.GCM;
			case "A":
				return DeviceType.APNS;
			case "B":
				return DeviceType.BAIDU;
		}
		throw new IllegalArgumentException("Error occured while pasing DeviceType from abbreviation! Argument :" + abbreviation);
	}
}

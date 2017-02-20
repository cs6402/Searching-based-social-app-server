package com.tdsoft.bro.dispatcher.service;

import com.tdsoft.bro.core.bean.DeviceInfoCacheBean;
import com.tdsoft.bro.core.entity.DeviceInfoEntity;


public interface IDeviceService {
	
	/**
	 * 註冊裝置
	 * @param die
	 * @param latitude
	 * @param longtitude
	 * @return 驗證令牌
	 */
	void registerDevice(DeviceInfoEntity die, double latitude, double longtitude);
	
	void validateDevice(String deviceId, String validationToken);
	
	/**
	 * 更西
	 * @param info
	 * @param locationEntity
	 */
	void updateLocation(DeviceInfoCacheBean info, double latitude, double longtitude);
	
	void refreshDevice(DeviceInfoEntity entity);
}

package com.tdsoft.bro.core.service.cache;

import com.tdsoft.bro.common.DeviceInfoCacheKeyType;
import com.tdsoft.bro.core.bean.DeviceInfoCacheBean;

public interface DeviceInfoCache extends CacheService {
	/**
	 * 將裝置資訊存入cache
	 * 
	 * @param deviceId 裝置代號
	 * @param bean 裝置資訊
	 * @param type 此次異動鍵值們
	 * @return 
	 */
	void putDeviceInfo(String deviceId, DeviceInfoCacheBean bean, DeviceInfoCacheKeyType... type);

	/**
	 * 根據裝置代號取回裝置資訊
	 * 
	 * @param deviceId
	 * @return 裝置訊息
	 */
	DeviceInfoCacheBean getDeviceInfo(String deviceId);


	/**
	 * 判斷是否裝置剩餘時間
	 * 
	 * @param deviceId 裝置代號
	 * @return Long 剩餘時間 (hour)
	 */
	Long getExireTime(String deviceId);

	/**
	 * 取得裝置代號與裝置類型的分隔符號，一般是@
	 * 
	 * @return 分隔符號
	 */
	String getDeviceIdSeparator();
	
	DeviceInfoCacheBean getDeviceInfoForPush(String deviceId);
	
	void putDeviceValidation(String deviceId, String vaildationToken);
	
	String getDeviceValidation(String deviceId);
}

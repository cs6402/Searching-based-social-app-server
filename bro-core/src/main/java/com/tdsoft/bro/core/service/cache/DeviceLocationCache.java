package com.tdsoft.bro.core.service.cache;

import java.util.List;

import com.tdsoft.bro.core.bean.DeviceLocationCacheBean;

public interface DeviceLocationCache extends CacheService {
	/**
	 * 裝置位置資訊存入cache
	 * 
	 * @param deviceLocationCacheBean 裝置位置物件
	 * @return 回傳地點代號
	 */
	Long putDeviceLocation(Double latitude, Double longtitude, String deviceId, DeviceLocationCacheBean deviceLocationCacheBean);

	/**
	 * 根據點與裝置代號取得裝置資訊
	 * 
	 * @param point 點
	 * @param deviceId 裝置代號
	 * @return 裝置類型 (1碼)+ SNSToken
	 */
	DeviceLocationCacheBean getDeviceLocation(Double latitude, Double longtitude, String deviceId);

	/**
	 * 根據點取得該點上所有裝置資訊
	 * @param radius 
	 * 
	 * @param point
	 * @return Map Key：裝置代號 , Value：裝置類型 (1碼)+ SNSToken
	 */
	List<DeviceLocationCacheBean> getDeviceLocationsByPoint(String point);


	/**
	 * 根據點與裝置代號刪除裝置資訊
	 * 
	 * @param point
	 * @param deviceId
	 */
	void delDeviceLocation(Double latitude, Double longtitude, String deviceId);
}

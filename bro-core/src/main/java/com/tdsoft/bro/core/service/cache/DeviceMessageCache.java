package com.tdsoft.bro.core.service.cache;

import java.util.List;

import com.tdsoft.bro.core.bean.DeviceMessageCacheBean;

public interface DeviceMessageCache extends CacheService {
	/**
	 * 裝置訊息存入cache
	 * 
	 * @param deviceIdAndDeviceType 裝置代號@裝置類型(數字)
	 * @param deviceMessageCacheBean 訊息物件
	 */
	void putDeviceMessage(String deviceIdAndDeviceType, DeviceMessageCacheBean deviceMessageCacheBean);

	/**
	 * 根據裝置代號取得裝置訊息
	 * 
	 * @param deviceIdAndDeviceType 裝置代號@裝置類型(數字)
	 * @param previouslyGetTimestamp 上次取得訊息時間
	 * @return
	 */
	List<DeviceMessageCacheBean> getDeviceMessage(String deviceIdAndDeviceType, Long previouslyGetTimestamp, Long page);

	/**
	 * 根據裝置代號刪除裝置資訊
	 * 
	 * @param deviceIdAndDeviceType 裝置代號@裝置類型(數字)
	 * @param timestamp 欲刪除最新訊息時間
	 */
	void delDeviceMessage(String deviceIdAndDeviceType, Long timestamp);
}

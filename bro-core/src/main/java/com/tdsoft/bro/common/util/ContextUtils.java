package com.tdsoft.bro.common.util;

import com.tdsoft.bro.core.bean.DeviceInfoCacheBean;

/**
 * 存放上下文使用到的物件
 * <h1>目前存放:</h1>
 * <ul>
 * <li> DeviceInfoCacheBean
 * </ul>
 * @author Daniel
 * 
 */
public class ContextUtils {

	private static ThreadLocal<DeviceInfoCacheBean> deviceInfo = new ThreadLocal<DeviceInfoCacheBean>();

	public static DeviceInfoCacheBean getDeviceInfo() {
		return deviceInfo.get();
	}

	public static void setDeviceInfo(DeviceInfoCacheBean deviceInfoCacheBean) {
		deviceInfo.set(deviceInfoCacheBean);
	}

	public static void clean() {
		deviceInfo.remove();
	}


}

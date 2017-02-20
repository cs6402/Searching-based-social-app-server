package com.tdsoft.bro.core.service.cache.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.tdsoft.bro.common.util.LocationUtils;
import com.tdsoft.bro.core.bean.DeviceLocationCacheBean;
import com.tdsoft.bro.core.service.cache.AbstractCacheService;
import com.tdsoft.bro.core.service.cache.DeviceLocationCache;

@Service("deviceLocationCacheImpl")
public class DeviceLocationCacheImpl extends AbstractCacheService implements DeviceLocationCache {

	@Resource(name = "deviceRedisClient")
	private RedisTemplate<String, String> deviceLocationRedisClient;

	/**
	 * DeviceLocation 該點Key前綴
	 */
	@Value("${core_cache_device_location_point_key}")
	private String deviceLocationPointKeyPrefix;

	@Override
	public Long putDeviceLocation(Double latitude, Double longtitude, String deviceId, DeviceLocationCacheBean deviceLocationCacheBean) {
		Long point = latLonToPoint(latitude, longtitude);
		String sPoint = appendPrefix(deviceLocationPointKeyPrefix, point.toString());
		// 先不用json格式做資料結構，單純使用+串接
		deviceLocationRedisClient.opsForHash().put(sPoint, deviceId, deviceLocationCacheBean.writeToCache());
		return point;
	}

	@Override
	public DeviceLocationCacheBean getDeviceLocation(Double latitude, Double longtitude, String deviceId) {
		String sPoint = convertAndAppendPrefix(latitude, longtitude);
		HashOperations<String, String, String> opsForHash = deviceLocationRedisClient.opsForHash();
		String value = opsForHash.get(sPoint, deviceId);
		// 先不用json格式做資料結構，單純使用+串接
		DeviceLocationCacheBean b = new DeviceLocationCacheBean();
		b.readFromCache(value);
		return b;
	}

	@Override
	public List<DeviceLocationCacheBean> getDeviceLocationsByPoint(String point) {
		point = appendPrefix(deviceLocationPointKeyPrefix, point);
		HashOperations<String, String, String> opsForHash = deviceLocationRedisClient.opsForHash();
		List<String> values = opsForHash.values(point);

		List<DeviceLocationCacheBean> list = new ArrayList<DeviceLocationCacheBean>();
		for (String value : values) {
			// 先不用json格式做資料結構，單純使用+串接
			DeviceLocationCacheBean b = new DeviceLocationCacheBean();
			b.readFromCache(value);
			list.add(b);
		}
		return list;
	}

	@Override
	public void delDeviceLocation(Double latitude, Double longtitude, String deviceId) {
		String sPoint = convertAndAppendPrefix(latitude, longtitude);
		deviceLocationRedisClient.opsForHash().delete(sPoint, deviceId);
	}

	private String convertAndAppendPrefix(Double latitude, Double longtitude) {
		Long point = latLonToPoint(latitude, longtitude);
		String sPoint = appendPrefix(deviceLocationPointKeyPrefix, point.toString());
		return sPoint;
	}
	
	private Long latLonToPoint(Double latitude, Double longtitude) {
		double[] pointXY = LocationUtils.convertLatLonToMercator(latitude, longtitude);
		long point = LocationUtils.convertMecratorToPoint(pointXY[0], pointXY[1]);
		return point;
	}
}

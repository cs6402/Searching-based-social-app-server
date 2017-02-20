package com.tdsoft.bro.core.service.cache.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

import com.tdsoft.bro.core.bean.DeviceMessageCacheBean;
import com.tdsoft.bro.core.service.cache.AbstractCacheService;
import com.tdsoft.bro.core.service.cache.DeviceMessageCache;
@Service("deviceSentMessageCacheImpl")
public class DeviceSentMessageCacheImpl extends AbstractCacheService implements DeviceMessageCache{

	
	@Resource(name = "messageRedisClient")
	private RedisTemplate<String, String> deviceMessageRedisClient;
	
	/**
	 * DeviceMessageKey後綴
	 */
	@Value("${core_cache_device_sent_message_point_key}")
	private String deviceMessageKeySuffix;
	
	@Override
	public void putDeviceMessage(String deviceIdAndDeviceType, DeviceMessageCacheBean deviceMessageCacheBean) {
		String key = appendSuffix(deviceMessageKeySuffix, deviceIdAndDeviceType);
		deviceMessageRedisClient.opsForZSet().add(key, deviceMessageCacheBean.getContent(), deviceMessageCacheBean.getTimestamp());
	}

	@Override
	public List<DeviceMessageCacheBean> getDeviceMessage(String deviceIdAndDeviceType, Long previouslyGetTimestamp, Long page) {
		String key = appendSuffix(deviceMessageKeySuffix, deviceIdAndDeviceType);
		// ignore page
		Set<TypedTuple<String>> rangeByScoreWithScores =
				deviceMessageRedisClient.opsForZSet().rangeByScoreWithScores(key, previouslyGetTimestamp, Long.MAX_VALUE, 0, 1);
		List<DeviceMessageCacheBean> list = new LinkedList<DeviceMessageCacheBean>();
		if (!rangeByScoreWithScores.isEmpty()) {
			DeviceMessageCacheBean bean = new DeviceMessageCacheBean();
			list.add(bean);
		}
		return list;
	}
	
	@Override
	public void delDeviceMessage(String deviceIdAndDeviceType, Long timestamp) {
		String key = appendSuffix(deviceMessageKeySuffix, deviceIdAndDeviceType);
		deviceMessageRedisClient.opsForZSet().removeRangeByScore(key, 0, timestamp);
	}

}

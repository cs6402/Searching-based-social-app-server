package com.tdsoft.bro.core.service.cache.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

import com.tdsoft.bro.core.bean.BroadcastMessageCacheBean;
import com.tdsoft.bro.core.service.cache.AbstractCacheService;
import com.tdsoft.bro.core.service.cache.BroMessageCache;

@Service("broMessageCacheImpl")
public class BroMessageCacheImpl extends AbstractCacheService implements BroMessageCache {

	@Resource(name = "messageRedisClient")
	private RedisTemplate<String, String> broadcastMessageRedisClient;

	/**
	 * BroadcastMessage 該點Key後綴
	 */
	@Value("${core_cache_broadcast_message_point_key}")
	private String broadcastMessagePointKeySuffix;
	
	/**
	 * BroadcastMessage 該點Key前綴
	 */
	@Value("${core_cache_device_location_point_key}")
	private String deviceLocationPointKeyPrefix;

	@Value("${core_cache_broadcast_message_max_count_per_query}")
	private int maxCountPerQuery;
	
	@Value("${core_cache_broadcast_message_max_score}")
	private long maxScore;
	
	@Override
	public void putBroadcastMessage(String point, BroadcastMessageCacheBean message) {
		// 存入訊息快取
		point = appendPrefix(deviceLocationPointKeyPrefix, point);
		point = appendSuffix(broadcastMessagePointKeySuffix, point);
		broadcastMessageRedisClient.opsForZSet().add(point, message.getContent(), message.getTimestamp());
	}

	@Override
	public void cleanBroadcastMessage(String point, Long timestamp) {
		// 刪除訊息快取
		point = appendPrefix(deviceLocationPointKeyPrefix, point);
		point = appendSuffix(broadcastMessagePointKeySuffix, point);
		broadcastMessageRedisClient.opsForZSet().removeRangeByScore(point, 0, timestamp);
	}

	@Override
	public List<BroadcastMessageCacheBean> getBroadcastMessaages(String point, Long previouslyGetTimestamp, Long page) {
		point = appendPrefix(deviceLocationPointKeyPrefix, point);
		point = appendSuffix(broadcastMessagePointKeySuffix, point);
		Set<TypedTuple<String>> rangeByScoreWithScores = broadcastMessageRedisClient.opsForZSet().rangeByScoreWithScores(point, previouslyGetTimestamp, maxScore, maxCountPerQuery * (page - 1) , maxCountPerQuery);
		List<BroadcastMessageCacheBean> list = new LinkedList<BroadcastMessageCacheBean>();
		for (TypedTuple<String> broadcastMessage : rangeByScoreWithScores) {
			BroadcastMessageCacheBean bean = new BroadcastMessageCacheBean();
			bean.setContent(broadcastMessage.getValue());
			bean.setTimestamp(broadcastMessage.getScore().longValue());
			list.add(bean);
		}
		return list; 
	}
}

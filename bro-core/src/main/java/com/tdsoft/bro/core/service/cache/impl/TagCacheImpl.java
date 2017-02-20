package com.tdsoft.bro.core.service.cache.impl;

import java.time.Instant;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

import com.google.common.base.Optional;
import com.tdsoft.bro.common.util.JsonUtils;
import com.tdsoft.bro.core.bean.TagCacheBean;
import com.tdsoft.bro.core.service.cache.AbstractCacheService;
import com.tdsoft.bro.core.service.cache.TagCache;

@Service("tagCacheImpl")
@ManagedResource(objectName = "annojmx:myjao=AnnotationObject", description = "MyJavaAnnotationObject")  
public class TagCacheImpl extends AbstractCacheService implements TagCache {

	private static final Logger logger = LoggerFactory.getLogger(TagCacheImpl.class);

	@Resource(name = "tagRedisClient")
	private RedisTemplate<String, String> tagRedisClient;

	@Value("${core_cache_tag_content_key}")
	private String tagContentKey;
	
	@Value("${core_cache_tag_temp_key}")
	private String tagTempKey;

	@Value("${core_cache_tag_lock_key}")
	private String tagLockKey;

	@Value("${core_cache_tag_lock_timeout}")
	private long lockTimeout;
	
	@Value("${core_cache_tag_waiting_time_to_get_token}")
	private long waittingTimeToGetToken;

	@Value("${core_cache_tag_retrieve_page_count}")
	private int count;
	
	@Override
	public void putTag(TagCacheBean bean) {
		// 註冊好的裝置tag 需放add ， 之後請放update
		String json = JsonUtils.convertObjectToJson(bean);
		// 存放每個裝置的tag資料
		// e.g: hashKey=1138819804752839813@B, value={D:1138819804752839813@B,tags=...etc}
		String deviceId = appendSuffix(tagContentKey, bean.getField().getDeviceId());
		tagRedisClient.opsForValue().set(deviceId, json);
		// 產生後蓋前的效果，避免短時間內頻繁設定tag導致搜索服務疲於重建索引
		// e.g: score=1442223803223, value=1138819804752839813@B
		tagRedisClient.opsForZSet().add(tagTempKey, bean.getField().getDeviceId(), Instant.now().getEpochSecond());
	}
	
	@Override
	public List<SimpleEntry<Double, String>> getTags(double timestamp, Long page) {
		// 取得時間點內有異動tag的裝置代號
		Set<TypedTuple<String>> deviceIdSet =
				tagRedisClient.opsForZSet().rangeByScoreWithScores(tagTempKey, 0, timestamp, (page - 1) * count, count);
		if (deviceIdSet.isEmpty()) {
			return Collections.emptyList();
		}
		Iterator<TypedTuple<String>> iterator = deviceIdSet.iterator();
		List<String> deviceIdList = new ArrayList<String>(deviceIdSet.size());
		List<Double> scoreList = new ArrayList<Double>(deviceIdSet.size());
		while (iterator.hasNext()) {
			TypedTuple<String> deviceIdWithScore = iterator.next();
			String deviceId = appendSuffix(tagContentKey, deviceIdWithScore.getValue());
			deviceIdList.add(deviceId);
			scoreList.add(deviceIdWithScore.getScore());
		}
		
		// 取得異動的tag內容，Json格式
		List<String> content = tagRedisClient.opsForValue().multiGet(deviceIdList);
		List<SimpleEntry<Double, String>> contentWithLatestScore = new ArrayList<SimpleEntry<Double, String>>(content.size());		
		for (int i = 0; i < content.size(); i++) {
			SimpleEntry<Double, String> entry = new SimpleEntry<Double, String>(scoreList.get(i), content.get(i));
			contentWithLatestScore.add(entry);
		}
		return contentWithLatestScore;
	}

	@Override
	public void removeTags(double timestamp) {
		tagRedisClient.opsForZSet().removeRangeByScore(tagTempKey, 0, timestamp);
	}

	@Override
	public String getTagToken() {
		ValueOperations<String, String> opsForValue = tagRedisClient.opsForValue();
		while (true) {
			// 取得新的任務執行期限(目前時間加上鎖定時間)
			String tokenValue = String.valueOf(Instant.now().getEpochSecond() + lockTimeout + 1l);
			// 能否取得執行token(將key放入Redis)
			Boolean isSuccessful = opsForValue.setIfAbsent(tagLockKey, tokenValue);
			if (isSuccessful) {
				return tokenValue;
			} else {
				// 放入失敗，判斷已存在任務是否超時
				String currentTokenMillis = opsForValue.get(tagLockKey);
				// 已存在的任務執行期限比現在時間少，代表某台機器任務超時
				if (Optional.fromNullable(currentTokenMillis).isPresent() && Long.parseLong(currentTokenMillis) < Instant.now().getEpochSecond()) {
					String newToken = String.valueOf(Instant.now().getEpochSecond() + lockTimeout + 1l);
					String oldToken = opsForValue.getAndSet(tagLockKey, newToken);
					if (oldToken.equals(currentTokenMillis)) {
						logger.info("Got token!{}", newToken);
						return newToken;
					}
				}
			}
			// 休息一下在重新檢查
			try {
				Thread.sleep(waittingTimeToGetToken);
			} catch (InterruptedException e) {
				logger.error("Waiting for retrieving tag token failed!", e);
			}
			logger.debug("Trying get token");
		}
	}

	@Override
	public void releaseTagToken() {
		tagRedisClient.opsForValue().getOperations().delete(tagLockKey);
	}

	@Override
	public TagCacheBean getTag(String deviceId) {
		deviceId = appendSuffix(tagContentKey, deviceId);
		String tagBeanContent = tagRedisClient.opsForValue().get(deviceId);
		TagCacheBean bean = JsonUtils.convertJsonToObject(tagBeanContent, TagCacheBean.class);
		return bean;
	}
}

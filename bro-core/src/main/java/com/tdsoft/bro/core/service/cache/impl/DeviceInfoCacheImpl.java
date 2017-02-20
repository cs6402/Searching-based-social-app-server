package com.tdsoft.bro.core.service.cache.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;
import com.tdsoft.bro.common.DeviceInfoCacheKeyType;
import com.tdsoft.bro.common.DeviceType;
import com.tdsoft.bro.core.bean.DeviceInfoCacheBean;
import com.tdsoft.bro.core.service.cache.AbstractCacheService;
import com.tdsoft.bro.core.service.cache.DeviceInfoCache;
import com.tdsoft.bro.core.service.cache.LocalCacheConstant;

@Service("deviceInfoCacheImpl")
public class DeviceInfoCacheImpl extends AbstractCacheService implements DeviceInfoCache {

	@Resource(name = "deviceRedisClient")
	private RedisTemplate<String, String> deviceInfoRedisClient;

	/**
	 * DeviceInfo TTL
	 */
	@Value("${core_cache_device_info_ttl}")
	private long deviceTimeout;

	/**
	 * DeviceInfo Key後綴
	 */
	@Value("${core_cache_device_info_key}")
	private String deviceInfoKeySuffix;

	/**
	 * DeviceInfo hash 點 field 名稱
	 */
	@Value("${core_cache_device_info_loc_lon_key}")
	private String deviceInfoCacheLocLonKey;
	@Value("${core_cache_device_info_loc_lat_key}")
	private String deviceInfoCacheLocLatKey;
	/**
	 * DeviceInfo hash snsToken field 名稱
	 */
	@Value("${core_cache_device_info_sns_token_key}")
	private String deviceInfoCacheSNSTokenKey;
	/**
	 * DeviceInfo hash 裝置序號 field 名稱
	 */
	@Value("${core_cache_device_info_no_key}")
	private String deviceInfoCacheNoKey;
	/**
	 * DeviceInfo hash UUID field 名稱
	 */
	@Value("${core_cache_device_info_uuid_key}")
	private String deviceInfoCacheUUIDKey;
	/**
	 * DeviceInfo hash 裝置類型  field 名稱
	 */
	@Value("${core_cache_device_info_type_key}")
	private String deviceInfoCacheTypeKey;
	
	/**
	 * DeviceInfo hash 外號  field 名稱
	 */
	@Value("${core_cache_device_info_alias_key}")
	private String deviceInfoCacheAliasKey;
	/**
	 * DeviceInfo hash 照片  field 名稱
	 */
	@Value("${core_cache_device_info_image_key}")
	private String deviceInfoCacheImageKey;
	
	@Value("${core_cache_device_info_id_separator}")
	private String deviceIdSeparator;
	
	/**
	 * 驗證用prefix
	 */
	@Value("${core_cache_device_info_validation_key}")
	private String deviceInfoCacheValidationKey;
	
	/**
	 * in sec
	 */
	@Value("${core_cache_device_info_validation_ttl}")
	private long deviceValidationTimeout;

	@Override
	@CacheEvict(value = {LocalCacheConstant.DEVICE_INFO_CACHE}, key = "#deviceId")
	public void putDeviceInfo(String deviceId, DeviceInfoCacheBean device, DeviceInfoCacheKeyType... types) {
		deviceId = appendSuffix(deviceInfoKeySuffix, deviceId);
		// FIXME too many round trip
		for (DeviceInfoCacheKeyType type : types) {
			switch (type) {
				case All:
					Map<String, Object> map = new HashMap<String, Object>();
					map.put(deviceInfoCacheNoKey, device.getDeviceNo().toString());
					map.put(deviceInfoCacheSNSTokenKey, device.getSnsToken());
					map.put(deviceInfoCacheLocLonKey, device.getLongtitude().toString());
					map.put(deviceInfoCacheLocLatKey, device.getLatitude().toString());
					map.put(deviceInfoCacheUUIDKey, device.getUuid());
					map.put(deviceInfoCacheTypeKey, device.getType().toString());
					map.put(deviceInfoCacheAliasKey, device.getAliasName());
					map.put(deviceInfoCacheImageKey, device.getImage());
					deviceInfoRedisClient.opsForHash().putAll(deviceId, map);
					break;
				case NO:
					deviceInfoRedisClient.opsForHash().put(deviceId, deviceInfoCacheNoKey, device.getDeviceNo().toString());
					break;
				case LOC_LON:
					deviceInfoRedisClient.opsForHash().put(deviceId, deviceInfoCacheLocLonKey, device.getLongtitude().toString());
					break;
				case LOC_LAT:
					deviceInfoRedisClient.opsForHash().put(deviceId, deviceInfoCacheLocLatKey, device.getLatitude().toString());
					break;
				case SNS_TOKEN:
					deviceInfoRedisClient.opsForHash().put(deviceId, deviceInfoCacheSNSTokenKey, device.getSnsToken());
					break;
				case UUID:
					deviceInfoRedisClient.opsForHash().put(deviceId, deviceInfoCacheUUIDKey, device.getUuid());
					break;
				case TYPE:
					deviceInfoRedisClient.opsForHash().put(deviceId, deviceInfoCacheTypeKey, device.getType().toString());
					break;
				case ALIAS:
					deviceInfoRedisClient.opsForHash().put(deviceId, deviceInfoCacheAliasKey, device.getAliasName());
					break;
				case IMAGE:
					deviceInfoRedisClient.opsForHash().put(deviceId, deviceInfoCacheImageKey, device.getImage());
					break;
				default:
					break;
			};
		}
	}

	@Override
	@Cacheable(LocalCacheConstant.DEVICE_INFO_CACHE)
	public DeviceInfoCacheBean getDeviceInfo(String deviceId) {
		HashOperations<String, String, String> opsForHash = deviceInfoRedisClient.opsForHash();

		String deviceIdWithPerfix = appendSuffix(deviceInfoKeySuffix, deviceId);
		// assemble all key set.
		ArrayList<String> keys = new ArrayList<String>();
		keys.add(deviceInfoCacheSNSTokenKey);
		keys.add(deviceInfoCacheLocLonKey);
		keys.add(deviceInfoCacheLocLatKey);
		keys.add(deviceInfoCacheUUIDKey);
		keys.add(deviceInfoCacheTypeKey);
		keys.add(deviceInfoCacheAliasKey);
		keys.add(deviceInfoCacheImageKey);
		
		// 避免裝置註冊時，不一致性，故將no放最後
		keys.add(deviceInfoCacheNoKey);

		List<String> multiGet = opsForHash.multiGet(deviceIdWithPerfix, keys);
		int i = 0;
		String value = multiGet.get(i++);
		if (Strings.isNullOrEmpty(value)) {
			// device info doesn't exist.
			return null;
		}
		DeviceInfoCacheBean bean = new DeviceInfoCacheBean();
		bean.setDeviceId(deviceId);
		bean.setSnsToken(value);
		value = multiGet.get(i++);
		bean.setLongtitude(Double.parseDouble(value));
		value = multiGet.get(i++);
		bean.setLatitude(Double.parseDouble(value));
		value = multiGet.get(i++);
		bean.setUuid(value);
		value = multiGet.get(i++);
		bean.setType(DeviceType.fromString(value));
		value = multiGet.get(i++);
		bean.setAliasName(value);
		value = multiGet.get(i);
		bean.setImage(value);
		value = multiGet.get(i);
		bean.setDeviceNo(Long.parseLong(value == null ? "0" : value));
		return bean;
	}

	@Override
	@Cacheable(LocalCacheConstant.DEVICE_INFO_EXPIRE_TIME_CACHE)
	public Long getExireTime(String deviceId) {
		return deviceInfoRedisClient.getExpire(appendSuffix(deviceInfoKeySuffix, deviceId), TimeUnit.HOURS);
	}
	
	@Override
	public String getDeviceIdSeparator() {
		return deviceIdSeparator;
	}

	@Override
	@Cacheable(LocalCacheConstant.DEVICE_INFO_PUSH_DATA_CACHE)
	public DeviceInfoCacheBean getDeviceInfoForPush(String deviceId) {
		HashOperations<String, String, String> opsForHash = deviceInfoRedisClient.opsForHash();

		String deviceIdWithPerfix = appendSuffix(deviceInfoKeySuffix, deviceId);
		// assemble all key set.
		ArrayList<String> keys = new ArrayList<String>();
		keys.add(deviceInfoCacheSNSTokenKey);
		keys.add(deviceInfoCacheTypeKey);

		List<String> multiGet = opsForHash.multiGet(deviceIdWithPerfix, keys);
		int i = 0;
		String value = multiGet.get(i++);
		if (Strings.isNullOrEmpty(value)) {
			// device info doesn't exist.
			return null;
		}
		DeviceInfoCacheBean bean = new DeviceInfoCacheBean();
		bean.setSnsToken(value);
		value = multiGet.get(i);
		bean.setType(DeviceType.fromString(value));
		return bean;
	}

	@Override
	public void putDeviceValidation(String deviceId, String vaildationToken) {
		String deviceIdWithPerfix = appendSuffix(deviceInfoCacheValidationKey, deviceId);
		deviceInfoRedisClient.opsForValue().set(deviceIdWithPerfix, vaildationToken, deviceValidationTimeout, TimeUnit.SECONDS);
	}

	@Override
	public String getDeviceValidation(String deviceId) {
		String deviceIdWithPerfix = appendSuffix(deviceInfoCacheValidationKey, deviceId);
		String vaildationToken = deviceInfoRedisClient.opsForValue().get(deviceIdWithPerfix);
		return vaildationToken;
	}

}

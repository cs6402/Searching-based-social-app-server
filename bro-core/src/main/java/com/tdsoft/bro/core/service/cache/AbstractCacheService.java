package com.tdsoft.bro.core.service.cache;

import com.tdsoft.bro.common.util.StringBuilderUtils;

public abstract class AbstractCacheService implements CacheService {
	/**
	 * 加上key前綴
	 * 
	 * @param key 愈加上前綴
	 * @return 已加完key
	 */
	protected String appendPrefix(String prefix, String key) {
		StringBuilder sb = StringBuilderUtils.getStringBuilder();
		sb.append(prefix).append(key);
		return sb.toString();
	}
	
	/**
	 * 加上key後綴
	 * 
	 * @param key 愈加上後綴
	 * @return 已加完key
	 */
	protected String appendSuffix(String suffix, String key) {
		StringBuilder sb = StringBuilderUtils.getStringBuilder();
		sb.append(key).append(suffix);
		return sb.toString();
	}
}

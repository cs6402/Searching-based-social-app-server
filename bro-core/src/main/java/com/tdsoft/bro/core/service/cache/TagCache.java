package com.tdsoft.bro.core.service.cache;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;

import com.tdsoft.bro.core.bean.TagCacheBean;

public interface TagCache extends CacheService {
	void putTag(TagCacheBean bean);

	TagCacheBean getTag(String deviceId);
	
	List<SimpleEntry<Double, String>> getTags(double timestamp, Long page);

	void removeTags(double timestamp);

	String getTagToken();

	void releaseTagToken();
}

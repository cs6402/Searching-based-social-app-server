package com.tdsoft.bro.core.service.search;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;

import com.tdsoft.bro.core.bean.TagSearchBean;
import com.tdsoft.bro.core.bean.TagSearchResultBean;

public interface TagSearch extends SearchService {
	
	List<SimpleEntry<String, String>> retrieveNearDeviceId(Double latitude, Double longtitude);

	List<TagSearchResultBean> retrieveNearTags(Double latitude, Double longtitude, Long page);

	List<TagSearchResultBean> searchTags(Double latitude, Double longtitude, String keyword, Long page);

	void postTags(byte[] documents, List<String> allDoc);
	
	List<TagSearchBean> retrieveNearDeviceInfosByTagSearch(Double latitude, Double longtitude, Long page);
}

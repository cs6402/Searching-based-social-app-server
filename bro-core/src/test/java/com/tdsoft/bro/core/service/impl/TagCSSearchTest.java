package com.tdsoft.bro.core.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.tdsoft.bro.common.util.JsonUtils;
import com.tdsoft.bro.core.bean.TagSearchBean;
import com.tdsoft.bro.core.bean.TagSearchResultBean;
import com.tdsoft.bro.core.service.cache.TagCache;
import com.tdsoft.bro.core.service.search.TagSearch;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-config.xml"})
public class TagCSSearchTest {

	 @Resource(name = "tagSearchImpl")
	TagSearch search;
	@Resource(name = "tagCacheImpl")
	TagCache cacheImpl;

//	@Test
	public void testGetToken() {
		String tagToken = cacheImpl.getTagToken();
		System.out.println(tagToken);
	}
//	 @Test
	public void testNearLocation() {
		for (long i = 0; i < 1; i++) {
			List<TagSearchBean> retrieveNearDeviceInfosByTagSearch = search.retrieveNearDeviceInfosByTagSearch(25.062320, 121.565045, ++i);
			System.out.println(retrieveNearDeviceInfosByTagSearch.size());
			System.out.println(JsonUtils.convertObjectToJson(retrieveNearDeviceInfosByTagSearch));
		}
	}
	
	
//	 @Test
	public void testLocation() {
		for (long i = 0; i < 1; i++) {
			List<SimpleEntry<String, String>> retrieveNearDeviceId = search.retrieveNearDeviceId(25.062320, 121.565045);
			System.out.println(retrieveNearDeviceId.size());
			System.out.println(JsonUtils.convertObjectToJson(retrieveNearDeviceId));
		}
	}

	 @Test
	public void testRetrieveNearTags() {
		// DeviceInfoCacheBean deviceInfoCacheBean = new DeviceInfoCacheBean();
		// deviceInfoCacheBean.setDeviceId("1138819804752832943@B");
		// ContextUtils.setDeviceInfo(deviceInfoCacheBean);
		for (long i = 0; i < 1; i++) {
			List<TagSearchResultBean> retrieveNearTags = search.retrieveNearTags(25.062320, 121.565045, ++i);
			System.out.println(retrieveNearTags.size());
			System.out.println(JsonUtils.convertObjectToJson(retrieveNearTags));
		}
	}

	// @Test
	public void testSearchTags() {
		for (long i = 0; i < 1; i++) {
			List<TagSearchResultBean> searchTags = search.searchTags(25.062320, 121.565045, "", ++i);
			System.out.println(searchTags.size());
			System.out.println(JsonUtils.convertObjectToJson(searchTags));
		}
	}

	// @Test
	public void testUpload() throws IOException {
		File f = new File("D:\\workspace\\abc.json");
		byte[] is = FileUtils.readFileToByteArray(f);
		search.postTags(is, null);
	}
}

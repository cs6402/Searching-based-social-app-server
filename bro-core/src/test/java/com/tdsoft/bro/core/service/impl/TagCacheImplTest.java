package com.tdsoft.bro.core.service.impl;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.tdsoft.bro.common.util.JsonUtils;
import com.tdsoft.bro.core.bean.TagCacheBean;
import com.tdsoft.bro.core.bean.TagESSearchBean;
import com.tdsoft.bro.core.bean.TagSearchBean;
import com.tdsoft.bro.core.service.cache.impl.TagCacheImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-config.xml"})
public class TagCacheImplTest {
	@Resource(name = "tagCacheImpl")
	public TagCacheImpl tagCache;

	@Test
	public void test() {
//		TagCacheBean tag = tagCache.getTag("5555555");
		List<SimpleEntry<Double, String>> tags = tagCache.getTags(1454426414933L, 1L);
		tags.forEach(tag->{
			System.out.println(JsonUtils.convertObjectToJson(tag));	
		});
		
//		TagCacheBean t = new TagCacheBean();
//		TagSearchBean ts = new TagESSearchBean();
//		ts.setAliasName("Test");
//		ts.setDeviceId("5555555");
//		ts.setImage("");
//		ts.setLatitude(0);
//		ts.setLongtitude(0);
//		ts.setPoint(9);
//		ts.setSnsToken("ttt");ts.setTags(new String[]{"Helo"});
//		t.setId("test");
//		t.setField(ts);
//		tagCache.putTag(t);
	}
}

package com.tdsoft.bro.core.service.impl;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.tdsoft.bro.common.util.JsonUtils;
import com.tdsoft.bro.core.bean.TagCSSearchBean;
import com.tdsoft.bro.core.bean.TagSearchBean;
import com.tdsoft.bro.core.service.search.TagSearch;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-config.xml"})
public class TagESSearchTest {

	 @Resource(name = "tagSearchImpl")
	TagSearch search;

	 @Test
	public void initIndex() {
		search.searchTags(25.059d, 121.563d, "呛",1l);
	}

}

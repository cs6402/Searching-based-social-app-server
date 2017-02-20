package com.tdsoft.bro.qhandler.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.tdsoft.bro.common.util.JsonUtils;
import com.tdsoft.bro.core.bean.AliasBean;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-config.xml"})
public class AliasServiceImplTest {
	@Resource(name = "defaultRedisClient")
	RedisTemplate<String, String> redisClient;

	@Test
	public void test() {
		String object = (String) redisClient.opsForHash().get("ALS", "DEFAULT");

		List<String> convertJsonToList = JsonUtils.convertJsonToList(object, String.class);
		List<AliasBean> as = new ArrayList<>();
		AtomicInteger a = new AtomicInteger(0);
		convertJsonToList.forEach(e -> {
			AliasBean ab = new AliasBean();
			ab.setName(e);
			ab.setImage(""+a.incrementAndGet());
			as.add(ab);
			
		});
		String convertObjectToJson = JsonUtils.convertObjectToJson(as);
		redisClient.opsForHash().put("ALS", "DEFAULT", convertObjectToJson);
		redisClient.opsForHash().put("ALS", "zh_TW", convertObjectToJson);
	}
}

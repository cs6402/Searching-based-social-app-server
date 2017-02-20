package com.tdsoft.bro.core.manager;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;

import com.google.common.base.Optional;
import com.tdsoft.bro.common.util.JsonUtils;
import com.tdsoft.bro.core.bean.AliasBean;


@Service("aliasManager")
public class AliasManager implements MessageListener {
	@Resource(name = "defaultRedisClient")
	RedisTemplate<String, String> redis;
	@Value("${manager_alias_key}")
	private String aliasKey;
	@Value("${manager_default_key}")
	private String defaultKey;
	
	private Map<String, List<AliasBean>> aliasesByLang;
	private Random random;

	@Resource(name = "messageListenerContainer")
	RedisMessageListenerContainer rmlc;

	@PostConstruct
	public void init() {
		random = new Random();
		rmlc.addMessageListener(this, new ChannelTopic(aliasKey));
		rmlc.start();
		initAlias();
	}

	private void initAlias() {
		HashOperations<String, String, String> opsForHash = redis.opsForHash();
		Map<String, String> entries = opsForHash.entries(aliasKey);
		Set<Entry<String, String>> entrySet = entries.entrySet();
		Map<String, List<AliasBean>> localMap = new ConcurrentHashMap<String, List<AliasBean>>();
		entrySet.forEach(e -> {
			String lang = e.getKey();
			String value = e.getValue();
			List<AliasBean> list = JsonUtils.convertJsonToList(value, AliasBean.class);
			localMap.put(lang, list);
		});
		aliasesByLang = localMap;
	}

	public AliasBean getAlias(String lang) {
		List<AliasBean> list = aliasesByLang.get(lang);
		if (!Optional.fromNullable(list).isPresent()) {
			list = aliasesByLang.get(defaultKey);
		}
		int n = random.nextInt(list.size()) + 1;
		return list.get(n);
	}

	public void refresh() {
		initAlias();
	}

	@Override
	public void onMessage(Message message, byte[] pattern) {
		refresh();
	}
}

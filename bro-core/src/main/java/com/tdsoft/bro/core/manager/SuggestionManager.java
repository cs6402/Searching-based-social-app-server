package com.tdsoft.bro.core.manager;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;

@Service("suggestionManager")
public class SuggestionManager implements MessageListener {
	@Resource(name = "defaultRedisClient")
	RedisTemplate<String, String> redis;
	@Value("${manager_suggestion_key}")
	private String suggestionKey;
	private Map<String, String> suggestionsByLang;

	@Value("${manager_default_key}")
	private String defaultKey;

	@Resource(name = "messageListenerContainer")
	RedisMessageListenerContainer rmlc;

	@PostConstruct
	public void init() {
		rmlc.addMessageListener(this, new ChannelTopic(suggestionKey));
		rmlc.start();
		initSuggestion();
	}

	private void initSuggestion() {
		HashOperations<String, String, String> opsForHash = redis.opsForHash();
		suggestionsByLang = opsForHash.entries(suggestionKey);
	}

	public void refresh() {
		initSuggestion();
	}

	/**
	 * 依據語言簡碼取得前十大熱門關鍵字
	 * 
	 * @param lang 語言簡碼 e.g en_US , zh_TW
	 * @return 十大熱門關鍵字
	 */
	public String getSuggestions(String lang) {
		String result = suggestionsByLang.get(lang);
		if (StringUtils.isNotBlank(result)) {
			return result;
		}
		return suggestionsByLang.get(defaultKey);
	}

	@Override
	public void onMessage(Message message, byte[] pattern) {
		refresh();
	}
}

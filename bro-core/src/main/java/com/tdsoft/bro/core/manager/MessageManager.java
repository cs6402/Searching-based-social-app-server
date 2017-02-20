package com.tdsoft.bro.core.manager;

import java.util.Map;
import java.util.UUID;
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
import org.springframework.web.context.request.async.PlusDeferredResult;
import org.springframework.web.socket.WebSocketSession;

import com.tdsoft.bro.core.service.cache.AbstractCacheService;

@Service("messageManager")
public class MessageManager extends AbstractCacheService implements MessageListener {

	@Resource(name = "messageRedisClient")
	private RedisTemplate<String, String> deviceMessageRedisClient;

	@Value("${core_cache_device_notify_message_point_key}")
	private String notifyMessageKey;

	@Value("${core_cache_device_service_key}")
	private String messageServicePrefix;

	/**
	 * 目前放置裝置用通知代號，未來可能新增web
	 */
	private static final String TYPE = "mobile";

	private Map<String, PlusDeferredResult<?>> retrieveChatMessageRequests;

	private Map<String, WebSocketSession> webSocketSessions;
	
	/**
	 * 服務器代號
	 */
	private String serviceKey;

	@Resource(name = "messageListenerContainer")
	RedisMessageListenerContainer rmlc;

	@PostConstruct
	public void init() {
		serviceKey = messageServicePrefix + UUID.randomUUID().toString();
		retrieveChatMessageRequests = new ConcurrentHashMap<String, PlusDeferredResult<?>>();
		webSocketSessions = new ConcurrentHashMap<>();
		rmlc.addMessageListener(this, new ChannelTopic(serviceKey));
		rmlc.start();
	}

	/**
	 * 設置等待通知的服務器內容
	 * 
	 * @param deviceId 裝置代號
	 * @param a
	 */
	public void needToNotify(String deviceId, PlusDeferredResult<?> a) {
		String key = appendSuffix(notifyMessageKey, deviceId);
		retrieveChatMessageRequests.put(deviceId, a);
		deviceMessageRedisClient.opsForHash().put(key, TYPE, serviceKey);
	}

	public void needToNotify(String deviceId, WebSocketSession webSocketSession) {
		String key = appendSuffix(notifyMessageKey, deviceId);
		webSocketSessions.put(deviceId, webSocketSession);
		deviceMessageRedisClient.opsForHash().put(key, TYPE, serviceKey);
	}


	/**
	 * 取得接收者目前的所在得服務器
	 * 
	 * @param deviceId 接收者裝置代號
	 * @return 服務器代號
	 */
	public Map<String, String> getNotifyInfo(String deviceId) {
		String key = appendSuffix(notifyMessageKey, deviceId);
		HashOperations<String, String, String> opsForHash = deviceMessageRedisClient.opsForHash();
		return opsForHash.entries(key);
	}

	/**
	 * 通知該服務器上的裝置解除等待
	 * 
	 * @param deviceId 裝置代號
	 * @param service 服務器代號
	 */
	public void notify(String service, String deviceId) {
		deviceMessageRedisClient.convertAndSend(service, deviceId);
	}

	/**
	 * 解除通知 並回傳解除成功與否
	 * 
	 * @param deviceId 裝置代號
	 * @return 是否解除成功，true 成功， false 失敗
	 */
	public boolean releaseNotify(String deviceId) {
		PlusDeferredResult<?> remove = retrieveChatMessageRequests.remove(deviceId);
		if (remove != null) {
			String key = appendSuffix(notifyMessageKey, deviceId);
			deviceMessageRedisClient.opsForHash().delete(key, TYPE);
			return true;
		}
		return false;
	}

	public String getServiceKey() {
		return serviceKey;
	}

	@Override
	public void onMessage(Message message, byte[] pattern) {
		String deviceId = new String(message.getBody());
		PlusDeferredResult<?> PlusDeferredResult = retrieveChatMessageRequests.get(deviceId);
		if (PlusDeferredResult != null) {
			PlusDeferredResult.getRetrieveMessageCallback().run();
			return;
		} 
		WebSocketSession webSocketSession = webSocketSessions.get(deviceId);
		if (webSocketSession != null) {
//			webSocketSession.sendMessage(message);
		}
		// FIXME 斷線處理
	}
}
